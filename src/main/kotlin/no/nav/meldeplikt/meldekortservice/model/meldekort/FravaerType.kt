package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

data class FravaerType (
    @JsonProperty("FraDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val fraDato: LocalDate,
    @JsonProperty("TilDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val tilDato: LocalDate,
    @JsonProperty("Type")
    val type: String
)