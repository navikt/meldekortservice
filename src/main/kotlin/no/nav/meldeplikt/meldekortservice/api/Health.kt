package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.healthApi() {

    route("/internal") {

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
