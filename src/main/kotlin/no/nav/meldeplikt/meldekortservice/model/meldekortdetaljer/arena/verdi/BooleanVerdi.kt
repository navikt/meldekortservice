package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class BooleanVerdi (
    @JacksonXmlProperty(localName = "Verdi")
    val verdi: Boolean
)