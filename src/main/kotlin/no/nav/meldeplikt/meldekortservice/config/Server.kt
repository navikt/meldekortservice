package no.nav.meldeplikt.meldekortservice.config

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.routing.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.cache.Cache
import no.nav.cache.CacheConfig
import no.nav.cache.CacheUtils
import no.nav.common.utils.EnvironmentUtils.Type.PUBLIC
import no.nav.common.utils.EnvironmentUtils.Type.SECRET
import no.nav.common.utils.EnvironmentUtils.setProperty
import no.nav.meldeplikt.meldekortservice.api.*
import no.nav.meldeplikt.meldekortservice.coroutine.SendJournalposterPaaNytt
import no.nav.meldeplikt.meldekortservice.database.OracleDatabase
import no.nav.meldeplikt.meldekortservice.database.PostgreSqlDatabase
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.service.KontrollService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.Contact
import no.nav.meldeplikt.meldekortservice.utils.swagger.Information
import no.nav.meldeplikt.meldekortservice.utils.swagger.Swagger
import no.nav.security.token.support.ktor.tokenValidationSupport

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val swagger = Swagger(
    info = Information(
        version = "1",
        title = "Meldekortservice",
        description = "Proxy-api for meldekort-applikasjonen (front-end). Api'et benyttes mot Arena og meldekortkontroll-api  \n" +
                "GitHub repo: [https://github.com/navikt/meldekortservice](https://github.com/navikt/meldekortservice)  \n" +
                "Slack: [#team-meldeplikt](https://nav-it.slack.com/archives/CQ61EHWP9)",
        contact = Contact(
            email = "meldeplikt@nav.no"
        )
    )
)


private const val cacheAntallMinutter = 55

// Årsaken til å multiplisere med 2 er at cache-implementasjonen dividerer timeout-verdien med 2...
private const val cacheTimeout: Long = cacheAntallMinutter.toLong() * 60 * 1000 * 2
val CACHE: Cache<String, AccessToken> = CacheUtils.buildCache(CacheConfig.DEFAULT.withTimeToLiveMillis(cacheTimeout))

const val SWAGGER_URL_V1 = "/meldekortservice/internal/apidocs/index.html?url=swagger.json"

@KtorExperimentalLocationsAPI
fun Application.mainModule(
    env: Environment = Environment(),
    mockDBService: DBService? = null,
    arenaOrdsService: ArenaOrdsService = ArenaOrdsService(),
    kontrollService: KontrollService = KontrollService(),
    dokarkivService: DokarkivService = DokarkivService(),
    mockFlywayConfig: org.flywaydb.core.Flyway? = null
) {
    setAppProperties(env)

    val dbService: DBService = mockDBService ?: initializeInnsendtMeldekortServiceApi(env)
    val flywayConfig: org.flywaydb.core.Flyway = mockFlywayConfig ?: initializeFlyway(env)

    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            JvmThreadMetrics(),
            ProcessorMetrics()
        )
    }

    install(DefaultHeaders)

    install(ContentNegotiation) {
        jackson { objectMapper }
    }

    val conf = this.environment.config
    install(Authentication) {
        if (isCurrentlyRunningOnNais()) {
            tokenValidationSupport(config = conf)
        } else {
            provider { skipWhen { true } }
        }
    }

    install(Locations)

    install(Routing) {
        healthApi(appMicrometerRegistry)
        swaggerRoutes()
        weblogicApi()
        meldekortApi(arenaOrdsService)
        personApi(arenaOrdsService, dbService, kontrollService, dokarkivService)
    }

    install(CallLogging) {
        filter { call -> call.request.path().startsWith("/api") }
    }

    flywayConfig.migrate()

    if (env.dokarkivResendInterval > 0L) {
        SendJournalposterPaaNytt(dbService, dokarkivService, env.dokarkivResendInterval, 0).start()
    }
}

private fun setAppProperties(environment: Environment) {
    setProperty(SOAP_STS_URL_KEY, environment.stsUrl, PUBLIC)
    setProperty(SOAP_SYSTEMUSER_USERNAME, environment.srvMeldekortservice.username, PUBLIC)
    setProperty(SOAP_SYSTEMUSER_PASSWORD, environment.srvMeldekortservice.password, SECRET)
    setProperty(SBL_ARBEID_USERNAME, environment.srvSblArbeid.username, PUBLIC)
    setProperty(SBL_ARBEID_PASSWORD, environment.srvSblArbeid.password, SECRET)
    setProperty(DB_ORACLE_USERNAME, environment.dbUserOracle.username, PUBLIC)
    setProperty(DB_ORACLE_PASSWORD, environment.dbUserOracle.password, SECRET)
    setProperty(DB_ORACLE_CONF, environment.dbConfOracle.jdbcUrl, PUBLIC)
}

private fun initializeInnsendtMeldekortServiceApi(env: Environment): DBService {
    return DBService(
        when (isCurrentlyRunningOnNais()) {
            true -> OracleDatabase()
            false -> PostgreSqlDatabase(env)
        }
    )
}

private fun initializeFlyway(env: Environment): org.flywaydb.core.Flyway {
    return Flyway.configure(env).load()
}
