package no.nav.meldeplikt.meldekortservice.model.meldekort

import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDate

data class FravaerType (
    @JacksonXmlProperty(localName = "FraDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val fraDato: LocalDate,
    @JacksonXmlProperty(localName = "TilDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val tilDato: LocalDate,
    @JacksonXmlProperty(localName = "Type")
    val type: String
)