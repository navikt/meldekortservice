package no.nav.meldeplikt.meldekortservice.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

data class Meldeperiode (
    @JacksonXmlProperty(localName = "Id")
    val id: String?,
    @JacksonXmlProperty(localName = "MeldeperiodeNavn")
    val meldeperiodeNavn: String,
    @JacksonXmlProperty(localName = "FraDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val fraDato: LocalDate,
    @JacksonXmlProperty(localName = "TilDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val tilDato: LocalDate
)