package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena

import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.BooleanVerdi
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Svar (
    @JacksonXmlProperty(localName = "SvarJa")
    val svarJa: BooleanVerdi,
    @JacksonXmlProperty(localName = "SvarNei")
    val svarNei: BooleanVerdi
)