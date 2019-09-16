package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.personApi(httpsClient: HttpClient) {

    route("/person") {

        // Henter historiske meldekort
        get("/historiskemeldekort") {
            call.respondText(text = "Historiske meldekort er kke implementert", contentType = ContentType.Text.Plain)
        }

        // Henter personstatus (arenastatus)
        get("/status") {
            call.respondText(text = "Status er ikke implementert", contentType = ContentType.Text.Plain)
        }

        // Henter person + meldekort
        get("/meldekort") {
            call.respondText(text = "Meldekort er ikke implementert", contentType = ContentType.Text.Plain)
        }
    }
}