package no.nav.meldeplikt.meldekortservice.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.document
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.request.uri
import io.ktor.response.respond
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
                    registerModule(ParameterNamesModule())
                    registerModule(Jdk8Module())
                    registerModule(JavaTimeModule())
                    enable(SerializationFeature.INDENT_OUTPUT)
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    dateFormat = StdDateFormat()
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

                    /*intercept(ApplicationCallPipeline.Setup) {
                        try {
                            *//*println("Intercept!")
                            val text = call.receiveText()
                            println(text)*//*
                            val url = call.request.uri
                            println("$url ble kalt!")
                            val text1 = call.receive<Any>()
                            println("Test: $text1")
                        } catch (e: Exception) {
                            println("Kunne ikke hente ut text: ${e.message}")
                            //call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                            //return@intercept finish()
                        }
                    }*/

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