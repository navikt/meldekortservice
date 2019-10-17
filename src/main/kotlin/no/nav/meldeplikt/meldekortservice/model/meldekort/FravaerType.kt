package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class FravaerType (
    @JsonProperty("FraDato")
    val fraDato: LocalDate,
    @JsonProperty("TilDato")
    val tilDato: LocalDate,
    @JsonProperty("Type")
    val type: String
)