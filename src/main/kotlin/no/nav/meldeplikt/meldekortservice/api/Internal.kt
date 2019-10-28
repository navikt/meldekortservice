package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import no.nav.meldeplikt.meldekortservice.config.SWAGGER_URL_V1
import no.nav.meldeplikt.meldekortservice.config.swagger
import no.nav.meldeplikt.meldekortservice.utils.API_PATH
import no.nav.meldeplikt.meldekortservice.utils.BASE_PATH
import no.nav.meldeplikt.meldekortservice.utils.INTERNAL_PATH
import no.nav.meldeplikt.meldekortservice.utils.swagger.SwaggerUi

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
            call.respondText(text = pingJsonResponse, contentType = ContentType.Application.Json)
        }
    }
}

@KtorExperimentalAPI
fun Routing.swaggerRoutes() {
    val swaggerUI = SwaggerUi()

    get(BASE_PATH) { call.respondRedirect(SWAGGER_URL_V1) }
    get(API_PATH) { call.respondRedirect(SWAGGER_URL_V1) }
    get("$INTERNAL_PATH/apidocs") { call.respondRedirect(SWAGGER_URL_V1) }
    get("$INTERNAL_PATH/apidocs/{fileName}") {
        val fileName = call.parameters["fileName"]
        if (fileName == "swagger.json") {
            call.respond(swagger)
        } else {
            swaggerUI.serve(fileName, call)
        }
    }
}