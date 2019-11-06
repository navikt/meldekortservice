package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

data class Hode (
    @JacksonXmlProperty(localName = "PersonId")
    val personId: LongVerdi,
    @JacksonXmlProperty(localName = "Fodselsnr")
    val fodselsnr: StringVerdi,
    @JacksonXmlProperty(localName = "MeldekortId")
    val meldekortId: LongVerdi,
    @JacksonXmlProperty(localName = "Meldeperiode")
    val meldeperiode: String,
    @JacksonXmlProperty(localName = "Arkivnokkel")
    val arkivnokkel: String,
    @JacksonXmlProperty(localName = "KortType")
    val kortType: String,
    @JacksonXmlProperty(localName = "MeldeDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val meldeDato: LocalDate? = null,
    @JacksonXmlProperty(localName = "LestDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val lestDato: LocalDate? = null
)

data class LongVerdi (
    @JacksonXmlProperty(localName = "Verdi")
    val verdi: Long
)

data class StringVerdi (
    @JacksonXmlProperty(localName = "Verdi")
    val verdi: String
)