package no.nav.meldeplikt.meldekortservice.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.ktor.http.HttpStatusCode

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdsStringResponse(
    val status: HttpStatusCode,
    val content: String
)