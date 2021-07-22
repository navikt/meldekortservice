package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.LongVerdi
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.StringVerdi
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Hode (
    @JacksonXmlProperty(localName = "PersonId")
    val personId: LongVerdi,
    @JacksonXmlProperty(localName = "Fodselsnr")
    val fodselsnr: StringVerdi,
    @JacksonXmlProperty(localName = "MeldekortId")
    val meldekortId: LongVerdi,
    @JacksonXmlProperty(localName = "Meldeperiode")
    val meldeperiode: String,
    @JacksonXmlProperty(localName = "Meldegruppe")
    val meldegruppe: String? ="",
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
    val lestDato: LocalDate? = null,
    @JacksonXmlProperty(localName = "Kommentar")
    val kommentar: String? = null
)