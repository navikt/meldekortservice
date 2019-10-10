package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente meldekortdetaljer og korrigering for en NAV-bruker.
 */
fun Route.meldekortApi(httpClient: HttpClient) {

    route("/meldekort") {

        //Intercepter request for å sjekke at id-parameteret er satt riktig
        intercept(ApplicationCallPipeline.Setup) {
            try {
                call.parameters["id"]!!.toLongOrNull() ?: throw IllegalArgumentException("Fant ikke id eller id var ikke et nummer")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                return@intercept finish()
            }
        }

        // Hent meldekortdetaljer
        get("/{id}") {
            val id = call.parameters["id"]!!.toLong()
            call.respondText(
                text = "Hent meldekortdetaljer er ikke implementert, men id var: $id",
                contentType = ContentType.Text.Plain
            )
        }

        //Henter meldekortid for nytt (korrigert) kort
        get("/{id}/korrigering") {
            val id = call.parameters["id"]!!.toLong()
            call.respondText(
                text = "Hent korrigert id er ikke implementert, men id var: $id",
                contentType = ContentType.Text.Plain
            )
        }
    }
}