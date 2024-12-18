package no.nav.meldeplikt.meldekortservice.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.meldeplikt.meldekortservice.utils.BASE_PATH
import no.nav.meldeplikt.meldekortservice.utils.INTERNAL_PATH
import no.nav.meldeplikt.meldekortservice.utils.SWAGGER_URL_V1
import no.nav.meldeplikt.meldekortservice.utils.swagger
import no.nav.meldeplikt.meldekortservice.utils.swagger.SwaggerUi

fun Route.healthApi(appMicrometerRegistry: PrometheusMeterRegistry) {

    route(INTERNAL_PATH) {
        val swaggerUI = SwaggerUi()

        get("") {
            call.respondRedirect(SWAGGER_URL_V1)
        }

        get("/") {
            call.respondRedirect(SWAGGER_URL_V1)
        }

        get("/apidocs") {
            call.respondRedirect(SWAGGER_URL_V1)
        }

        get("/apidocs/") {
            call.respondRedirect(SWAGGER_URL_V1)
        }

        get("/apidocs/{fileName}") {
            val fileName = call.parameters["fileName"]
            if (fileName == "swagger.json") {
                call.respond(swagger)
            } else {
                swaggerUI.serve(fileName, call)
            }
        }

        get("/isAlive") {
            call.respondText("Alive")
        }

        get("/isReady") {
            call.respondText("Ready")
        }

        get("/ping") {
            val pingJsonResponse = """{"ping": "pong"}"""
            call.respondText(text = pingJsonResponse, contentType = ContentType.Application.Json)
        }

        get("/metrics") {
            call.respondText(appMicrometerRegistry.scrape())
        }
    }
}

fun Routing.swaggerRoutes() {
    route(BASE_PATH) {
        get("") {
            call.respondRedirect(SWAGGER_URL_V1)
        }

        get("/") {
            call.respondRedirect(SWAGGER_URL_V1)
        }

        get("/api") {
            call.respondRedirect(SWAGGER_URL_V1)
        }

        get("/api/") {
            call.respondRedirect(SWAGGER_URL_V1)
        }
    }
}
