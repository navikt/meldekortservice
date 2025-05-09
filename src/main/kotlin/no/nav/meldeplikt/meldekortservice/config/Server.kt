package no.nav.meldeplikt.meldekortservice.config

import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.basic
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.common.utils.EnvironmentUtils.Type.PUBLIC
import no.nav.common.utils.EnvironmentUtils.Type.SECRET
import no.nav.common.utils.EnvironmentUtils.setProperty
import no.nav.meldeplikt.meldekortservice.api.*
import no.nav.meldeplikt.meldekortservice.database.OracleDatabase
import no.nav.meldeplikt.meldekortservice.database.PostgreSqlDatabase
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.security.token.support.v3.tokenValidationSupport
import org.slf4j.event.Level

lateinit var defaultDbService: DBService

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.mainModule(
    env: Environment = Environment(),
    mockDBService: DBService? = null,
    mockFlywayConfig: org.flywaydb.core.Flyway? = null,
    mockArenaOrdsService: ArenaOrdsService? = null
) {
    setAppProperties(env)

    defaultDbService = mockDBService ?: initializeDbService(env)

    val flywayConfig: org.flywaydb.core.Flyway = mockFlywayConfig ?: initializeFlyway(env)
    flywayConfig.migrate()

    val arenaOrdsService = mockArenaOrdsService ?: ArenaOrdsService()

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

    install(Resources)

    routing {
        healthApi(appMicrometerRegistry)
        swaggerRoutes()
        skrivemodusApi(arenaOrdsService)
        meldekortApi(arenaOrdsService)
        personApi(arenaOrdsService)
        meldekortApiV2(arenaOrdsService)
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
}

private fun setAppProperties(environment: Environment) {
    setProperty(DB_ORACLE_USERNAME, environment.dbUserOracle.username, PUBLIC)
    setProperty(DB_ORACLE_PASSWORD, environment.dbUserOracle.password, SECRET)
    setProperty(DB_ORACLE_CONF, environment.dbConfOracle.jdbcUrl, PUBLIC)
}

private fun initializeDbService(env: Environment): DBService {
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
