package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.meldeplikt.meldekortservice.config.SWAGGER_URL_V1
import no.nav.meldeplikt.meldekortservice.config.swagger
import no.nav.meldeplikt.meldekortservice.utils.API_PATH
import no.nav.meldeplikt.meldekortservice.utils.BASE_PATH
import no.nav.meldeplikt.meldekortservice.utils.INTERNAL_PATH
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
            call.respondText(text = "Alive", contentType = ContentType.Text.Plain)
        }

        get("/isReady") {
            call.respondText(text = "Ready", contentType = ContentType.Text.Plain)
        }

        get("/ping") {
            val pingJsonResponse = """{"ping": "pong"}"""
            call.respondText(text = pingJsonResponse, contentType = ContentType.Application.Json)
        }

        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
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
