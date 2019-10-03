package no.nav.meldeplikt.meldekortservice.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import io.prometheus.client.hotspot.DefaultExports
import no.nav.meldeplikt.meldekortservice.api.healthApi
import no.nav.meldeplikt.meldekortservice.api.meldekortApi
import no.nav.meldeplikt.meldekortservice.api.personApi
import no.nav.meldeplikt.meldekortservice.api.testApi
import no.nav.meldeplikt.meldekortservice.config.ConfigUtil.isCurrentlyRunningOnNais
import java.util.concurrent.TimeUnit

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
                }
            }

            install(Authentication) {
                jwt {
                    setupOidcAuthentication(environment)
                }
            }

            routing {
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