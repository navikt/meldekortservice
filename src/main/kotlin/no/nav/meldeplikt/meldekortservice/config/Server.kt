package no.nav.meldeplikt.meldekortservice.config

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.locations.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.common.utils.EnvironmentUtils.Type.PUBLIC
import no.nav.common.utils.EnvironmentUtils.Type.SECRET
import no.nav.common.utils.EnvironmentUtils.setProperty
import no.nav.meldeplikt.meldekortservice.api.*
import no.nav.meldeplikt.meldekortservice.coroutine.SendJournalposterPaaNytt
import no.nav.meldeplikt.meldekortservice.database.OracleDatabase
import no.nav.meldeplikt.meldekortservice.database.PostgreSqlDatabase
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.service.KontrollService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.security.token.support.v2.tokenValidationSupport
import org.slf4j.event.Level

lateinit var defaultDbService: DBService

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalLocationsAPI
fun Application.mainModule(
    env: Environment = Environment(),
    mockDBService: DBService? = null,
    mockFlywayConfig: org.flywaydb.core.Flyway? = null,
    mockArenaOrdsService: ArenaOrdsService? = null,
    mockKontrollService: KontrollService? = null,
    mockDokarkivService: DokarkivService? = null
) {
    setAppProperties(env)

    defaultDbService = mockDBService ?: initializeInnsendtMeldekortServiceApi(env)

    val flywayConfig: org.flywaydb.core.Flyway = mockFlywayConfig ?: initializeFlyway(env)
    flywayConfig.migrate()

    val arenaOrdsService = mockArenaOrdsService ?: ArenaOrdsService()
    val kontrollService = mockKontrollService ?: KontrollService()
    val dokarkivService = mockDokarkivService ?: DokarkivService()

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
        jackson { defaultObjectMapper }
    }

    val conf = this.environment.config
    install(Authentication) {
        if (isCurrentlyRunningOnNais()) {
            tokenValidationSupport(config = conf)
        } else {
            basic {
                skipWhen { true }
            }
        }
    }

    install(Locations)

    install(Routing) {
        healthApi(appMicrometerRegistry)
        swaggerRoutes()
        skrivemodusApi(arenaOrdsService)
        meldekortApi(arenaOrdsService)
        personApi(arenaOrdsService, defaultDbService, kontrollService, dokarkivService)
    }

    install(DoubleReceive) {
    }

    install(CallId) {
        // Retrieve the callId from a headerName
        // Automatically updates the response with the callId in the specified headerName
        header(HttpHeaders.XRequestId)

        // If can't retrieve a callId from the ApplicationCall, it will try the generate-blocks coalescing until one of them is not null.
        generate { generateCallId() }

        // Once a callId is generated, this optional function is called to verify if the retrieved or generated callId String is valid.
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }

    install(CallLogging) {
        // Specifies what level will messages from this plugin have, we set DEBUG to get rid of them during normal work
        level = Level.DEBUG

        // By default, this plugin tries to have console colors (ANSI escape codes) in its messages. Turn it off
        disableDefaultColors()

        // Put callId into MDC
        callIdMdc(MDC_CORRELATION_ID)
    }

    install(IncomingCallLoggingPlugin) {
        dbs = defaultDbService
    }

    if (env.dokarkivResendInterval > 0L) {
        SendJournalposterPaaNytt(defaultDbService, dokarkivService, env.dokarkivResendInterval, 0).start()
    }
}

private fun setAppProperties(environment: Environment) {
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
