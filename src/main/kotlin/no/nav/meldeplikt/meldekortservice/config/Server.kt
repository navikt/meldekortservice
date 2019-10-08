package no.nav.meldeplikt.meldekortservice.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.prometheus.client.hotspot.DefaultExports
import no.nav.meldeplikt.meldekortservice.api.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.Contact
import no.nav.meldeplikt.meldekortservice.utils.swagger.Information
import no.nav.meldeplikt.meldekortservice.utils.swagger.Swagger
import no.nav.meldeplikt.meldekortservice.utils.swagger.SwaggerUi
import no.nav.meldeplikt.meldekortservice.utils.API_PATH
import no.nav.meldeplikt.meldekortservice.utils.BASE_PATH
import no.nav.meldeplikt.meldekortservice.utils.INTERNAL_PATH
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
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

const val SWAGGER_URL_V1 = "/meldekortservice/internal/apidocs/index.html?url=swagger.json"

object Server {

    private const val portNumber = 8090
    private const val basePath = "/meldekortservice"

    fun configure(environment: Environment): NettyApplicationEngine {
        DefaultExports.initialize()
        val client = HttpClient().client
        val app = embeddedServer(Netty, port = portNumber) {
            install(DefaultHeaders)

            install(ContentNegotiation) {
                jackson {
                    registerModule(KotlinModule())
                    enable(SerializationFeature.INDENT_OUTPUT)
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    setSerializationInclusion(JsonInclude.Include.NON_NULL)
                }
            }

            install(Authentication) {
                jwt {
                    setupOidcAuthentication(environment)
                }
            }

            install(Locations)

            val swaggerUI = SwaggerUi()

            install(Routing) {
                get(BASE_PATH) { call.respondRedirect(SWAGGER_URL_V1) }
                get(API_PATH) { call.respondRedirect(SWAGGER_URL_V1) }
                get("$INTERNAL_PATH/apidocs") { call.respondRedirect(SWAGGER_URL_V1) }
                get("$INTERNAL_PATH/apidocs/{fileName}") {
                    val fileName = call.parameters["fileName"]
                    if (fileName == "swagger.json") call.respond(swagger) else swaggerUI.serve(fileName, call)
                }

                healthApi()

                meldekortApi(client)
                personApi(client)
            }

            /*routing {
                // swagger UI trigger routes

                route(basePath) {
                    healthApi()

                    // Midlertidig oppsett for å lettere kunne teste lokalt
                    if (isCurrentlyRunningOnNais()) {
                        authenticate {
                            testApi()
                            personApi(client)
                            meldekortApi(client)
                        }
                    } else {
                        testApi()
                        personApi(client)
                        meldekortApi(client)
                    }
                }
            }*/
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