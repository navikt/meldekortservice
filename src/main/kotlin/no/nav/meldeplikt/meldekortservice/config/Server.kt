package no.nav.meldeplikt.meldekortservice.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import no.nav.cache.Cache
import no.nav.cache.CacheConfig
import no.nav.cache.CacheUtils
import no.nav.meldeplikt.meldekortservice.api.*
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.utils.swagger.Contact
import no.nav.meldeplikt.meldekortservice.utils.swagger.Information
import no.nav.meldeplikt.meldekortservice.utils.swagger.Swagger
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.meldeplikt.meldekortservice.utils.objectMapper
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants
import no.nav.sbl.util.EnvironmentUtils.setProperty
import no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC
import no.nav.sbl.util.EnvironmentUtils.Type.SECRET
import java.util.concurrent.TimeUnit

val swagger = Swagger(
    info = Information (
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

object Server {

    private const val portNumber = 8090
    private const val basePath = "/meldekortservice"

    @KtorExperimentalLocationsAPI
    @KtorExperimentalAPI
    fun configure(environment: Environment): NettyApplicationEngine {

        DefaultExports.initialize()
        setAppProperties(environment)
        val app = embeddedServer(Netty, port = portNumber) {
            install(DefaultHeaders)

            install(ContentNegotiation) {
                jackson { objectMapper }
            }

            install(Authentication) {
                jwt {
                    setupOidcAuthentication(environment)
                }
            }

            install(Locations)

            install(Routing) {
                healthApi()
                swaggerRoutes()
                weblogicApi()
                meldekortApi()
                personApi()
            }
        }
        //configureStartupHook()
        Flyway.runFlywayMigrations(environment)
        addGraceTimeAtShutdownToAllowRunningRequestsToComplete(app)
        return app
    }

    private fun addGraceTimeAtShutdownToAllowRunningRequestsToComplete(app: NettyApplicationEngine) {
        if(isCurrentlyRunningOnNais()) {
            Runtime.getRuntime().addShutdownHook(Thread {
                app.stop(5, 60, TimeUnit.SECONDS)
            })
        }
    }

    private fun setAppProperties(environment: Environment) {
        val systemuser = hentVaultCredentials()
        setProperty(StsSecurityConstants.STS_URL_KEY, environment.securityTokenService, PUBLIC)
        setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, systemuser.username, PUBLIC)
        setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, systemuser.password, SECRET)
    }

    private fun Application.configureStartupHook(env: Environment) {
        environment.monitor.subscribe(ApplicationStarted) {
            Flyway.runFlywayMigrations(env)
        }
    }
}