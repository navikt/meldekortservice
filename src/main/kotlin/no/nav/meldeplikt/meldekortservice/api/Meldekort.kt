package no.nav.meldeplikt.meldekortservice.api

import io.ktor.client.HttpClient
import io.ktor.routing.Route
import io.ktor.routing.route

fun Route.meldekortApi(httpClient: HttpClient) {

    route("/meldekort") {

    }
}