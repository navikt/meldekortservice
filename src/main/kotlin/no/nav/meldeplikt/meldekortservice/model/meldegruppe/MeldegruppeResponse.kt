package no.nav.meldeplikt.meldekortservice.model.meldegruppe

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MeldegruppeResponse(
    val meldegruppeListe: List<Meldegruppe>
)
