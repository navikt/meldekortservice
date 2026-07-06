package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
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
    val mottattDato: LocalDate? = null,
    @JacksonXmlProperty(localName = "BruttoBelop")
    val bruttoBelop: Float = 0F
)