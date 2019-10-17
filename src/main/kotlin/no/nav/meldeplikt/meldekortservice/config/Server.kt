package no.nav.meldeplikt.meldekortservice.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
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

private val cacheAntallMinutter = 60
// Årsaken til å multiplisere med 2 er at cache-implementasjonen dividerer timeout-verdien med 2...
private val cacheTimeout: Long = cacheAntallMinutter.toLong() * 60 * 1000 * 2
val cache: Cache<String, OrdsToken> = CacheUtils.buildCache(CacheConfig.DEFAULT.withTimeToLiveMillis(cacheTimeout))

val client = HttpClient().client

const val SWAGGER_URL_V1 = "/meldekortservice/internal/apidocs/index.html?url=swagger.json"

object Server {

    private const val portNumber = 8090
    private const val basePath = "/meldekortservice"

    @KtorExperimentalLocationsAPI
    @KtorExperimentalAPI
    fun configure(environment: Environment): NettyApplicationEngine {
        DefaultExports.initialize()
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
                meldekortApi(client)
                personApi(client)
            }
        }
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
}