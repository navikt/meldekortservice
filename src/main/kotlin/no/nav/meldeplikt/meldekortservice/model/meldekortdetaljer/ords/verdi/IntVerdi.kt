package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.verdi

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class IntVerdi (
    @JacksonXmlProperty(localName = "Verdi")
    val verdi: Int
)