package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class Meldekort (
    @JacksonXmlProperty(localName = "Hode")
    val hode: Hode,
    @JacksonXmlProperty(localName = "Spm")
    val spm: Spm
)