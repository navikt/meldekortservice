package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import no.nav.meldeplikt.meldekortservice.config.extractIdentFromLoginContext

fun Route.testApi() {

    route("/test") {

        get("/tokenTest") {
            call.respondText(text = "Nice! Du kom gjennom. Ident ${extractIdentFromLoginContext()}", contentType = ContentType.Text.Plain)
        }
    }
}