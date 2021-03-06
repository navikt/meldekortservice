package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.BooleanVerdi

data class Svar (
    @JacksonXmlProperty(localName = "SvarJa")
    val svarJa: BooleanVerdi,
    @JacksonXmlProperty(localName = "SvarNei")
    val svarNei: BooleanVerdi
)