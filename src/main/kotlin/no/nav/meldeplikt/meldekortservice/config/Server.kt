package no.nav.meldeplikt.meldekortservice.config

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.prometheus.client.hotspot.DefaultExports
import no.nav.cache.Cache
import no.nav.cache.CacheConfig
import no.nav.cache.CacheUtils
import no.nav.meldeplikt.meldekortservice.api.*
import no.nav.meldeplikt.meldekortservice.database.OracleDatabase
import no.nav.meldeplikt.meldekortservice.database.PostgreSqlDatabase
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.InnsendtMeldekortService
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
        title = "Meldekortservice. Proxy-api for meldekort mot Arena og Amelding",
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
val cache: Cache<String, OrdsToken> = CacheUtils.buildCache(CacheConfig.DEFAULT.withTimeToLiveMillis(cacheTimeout))

const val SWAGGER_URL_V1 = "/meldekortservice/internal/apidocs/index.html?url=swagger.json"

fun Application.mainModule(env: Environment = Environment()) {

    DefaultExports.initialize()
    setAppProperties(env)
    val innsendtMeldekortService = InnsendtMeldekortService(
        when (isCurrentlyRunningOnNais()) {
            true -> OracleDatabase()
            false -> PostgreSqlDatabase(env)
        }
    )
    val arenaOrdsService = ArenaOrdsService()

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
        personApi(arenaOrdsService, innsendtMeldekortService)
    }
    Flyway.runFlywayMigrations(env)
}

private fun setAppProperties(environment: Environment) {
    val systemuser = hentVaultCredentials(environment.serviceUserKvPath)
    val srvSblArbeid = hentVaultCredentials(environment.srvSblArbeidPath)
    val dbUserOracle = hentVaultCredentials(environment.dbUserOracleKvPath)
    val dbConfOracle = hentVaultDbConfig(environment.dbConfOracleKvPath)

    setProperty(StsSecurityConstants.STS_URL_KEY, environment.securityTokenService, PUBLIC)
    setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, systemuser.username, PUBLIC)
    setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, systemuser.password, SECRET)
    setProperty(SBL_ARBEID_USERNAME, srvSblArbeid.username, PUBLIC)
    setProperty(SBL_ARBEID_PASSWORD, srvSblArbeid.password, SECRET)
    setProperty(DB_ORACLE_USERNAME, dbUserOracle.username, PUBLIC)
    setProperty(DB_ORACLE_PASSWORD, dbUserOracle.password, SECRET)
    setProperty(DB_ORACLE_CONF, dbConfOracle.jdbcUrl, PUBLIC)
}
