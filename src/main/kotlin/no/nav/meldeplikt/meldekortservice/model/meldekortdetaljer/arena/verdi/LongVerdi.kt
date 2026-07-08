package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi

import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class LongVerdi (
    @JacksonXmlProperty(localName = "Verdi")
    val verdi: Long
)