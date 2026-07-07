package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Person (
    @JacksonXmlProperty(localName = "PersonId")
    val personId: Long,
    @JacksonXmlProperty(localName = "Etternavn")
    val etternavn: String,
    @JacksonXmlProperty(localName = "Fornavn")
    val fornavn: String,
    @JacksonXmlProperty(localName = "Maalformkode")
    val maalformkode: String,
    @JacksonXmlProperty(localName = "Meldeform")
    val meldeform: String,
    @JacksonXmlProperty(localName = "MeldekortListe")
    val meldekortListe: List<Meldekort>? = null,
    @JacksonXmlProperty(localName = "AntallGjenstaaendeFeriedager")
    val antallGjenstaaendeFeriedager: Int? = 0,
    @JacksonXmlProperty(localName = "FravaerListe")
    val fravaerListe: List<FravaerType>? = null
)