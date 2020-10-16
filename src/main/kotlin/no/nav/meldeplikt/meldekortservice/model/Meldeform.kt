package no.nav.meldeplikt.meldekortservice.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Meldeform constructor (
    val meldeformNavn: String
)