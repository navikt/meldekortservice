package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import no.aetat.arena.mk_meldekort.MeldekortType
import no.nav.meldeplikt.meldekortservice.config.Amelding
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.model.Meldekortdetaljer

fun Route.personApi(httpClient: HttpClient) {

    route("/person") {

        val environment = Environment()

        // Henter historiske meldekort
        get("/historiskemeldekort") {
            call.respondText(text = "Historiske meldekort er ikke implementert", contentType = ContentType.Text.Plain)
        }

        // Henter personstatus (arenastatus)
        get("/status") {
            call.respondText(text = "Status er ikke implementert", contentType = ContentType.Text.Plain)
        }

        // Henter person + meldekort
        get("/meldekort") {
            call.respondText(text = "Meldekort er ikke implementert", contentType = ContentType.Text.Plain)
        }

        post("/meldekort") {
            val meldekort = call.receive<Meldekortdetaljer>()
            println("Meldekortdetaljer: $meldekort")
            val kontrollertType = Amelding.ameldingService(environment).kontrollerMeldekort(meldekort)

            if (kontrollertType.status == "OK") {
                call.respond(status = HttpStatusCode.OK, message = kontrollertType)
            } else {
                call.respond(status = HttpStatusCode.BadRequest, message = "Meldekort ble ikke sendt inn")
            }
        }
    }
}