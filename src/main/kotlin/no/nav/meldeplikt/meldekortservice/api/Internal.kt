package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import no.nav.meldeplikt.meldekortservice.utils.INTERNAL_PATH

fun Route.healthApi() {

    route(INTERNAL_PATH) {

        get("/isAlive") {
            call.respondText(text = "Alive", contentType = ContentType.Text.Plain)
        }

        get("/isReady") {
            call.respondText(text = "Ready", contentType = ContentType.Text.Plain)
        }

        get("/ping") {
            val pingJsonResponse = """{"ping": "pong"}"""
            call.respondText(pingJsonResponse, ContentType.Application.Json)
        }
    }
}

/*
fun Route.swaggerRoutes() {

    val swaggerUI = SwaggerUi()
    val SWAGGER_URL_V1 = "/meldekortservice/internal/apidocs/index.html?url=swagger.json"

    route("/internal") {
        get("/swagger") {
            println("inni swagger-endepukt")
            swaggerUI.serve("swagger.json", call)
        }
    }
}*/
