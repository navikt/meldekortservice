package no.nav.meldeplikt.meldekortservice.config

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.routing.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.cache.Cache
import no.nav.cache.CacheConfig
import no.nav.cache.CacheUtils
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
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants
import no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC
import no.nav.sbl.util.EnvironmentUtils.Type.SECRET
import no.nav.sbl.util.EnvironmentUtils.setProperty
import no.nav.security.token.support.ktor.tokenValidationSupport

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val swagger = Swagger(
    info = Information(
        version = "1",
        title = "Meldekortservice. Proxy-api for meldekort-applikasjonen (front-end). Api'et benyttes mot Arena og meldekortkontroll-api",
        description = "[Meldekortservice](https://github.com/navikt/meldekortservice)",
        contact = Contact(
            name = "#meldeplikt på Slack",
            url = "https://github.com/navikt/meldekortservice",
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

    DefaultExports.initialize()
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
        healthApi()
        swaggerRoutes()
        weblogicApi()
        meldekortApi(arenaOrdsService)
        personApi(arenaOrdsService, dbService, kontrollService, dokarkivService)
        tekstApi(dbService)
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
    setProperty(StsSecurityConstants.STS_URL_KEY, environment.stsUrl, PUBLIC)
    setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, environment.srvMeldekortservice.username, PUBLIC)
    setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, environment.srvMeldekortservice.password, SECRET)
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

