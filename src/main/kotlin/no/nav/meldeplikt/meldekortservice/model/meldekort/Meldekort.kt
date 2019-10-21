package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

data class Meldekort (
    @JacksonXmlProperty(localName = "MeldekortId")
    val meldekortId: Long,
    @JacksonXmlProperty(localName = "KortType")
    val kortType: String,
    @JacksonXmlProperty(localName = "Meldeperiode")
    val meldeperiode: String,
    @JacksonXmlProperty(localName = "FraDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val fraDato: LocalDate,
    @JacksonXmlProperty(localName = "TilDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val tilDato: LocalDate,
    @JacksonXmlProperty(localName = "HoyesteMeldegruppe")
    val hoyesteMeldegruppe: String,
    @JacksonXmlProperty(localName = "Beregningstatus")
    val beregningstatus: String,
    @JacksonXmlProperty(localName = "Forskudd")
    val forskudd: Boolean,
    @JacksonXmlProperty(localName = "MottattDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val mottattDato: LocalDate?,
    @JacksonXmlProperty(localName = "BruttoBelop")
    val bruttoBelop: String?
)