package no.nav.meldeplikt.meldekortservice.model.response

import io.ktor.http.HttpStatusCode

data class OrdsStringResponse(
    val status: HttpStatusCode,
    val content: String
)