package no.nav.meldeplikt.meldekortservice.model.korriger

import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class KopierMeldekortResponse(
    @JacksonXmlProperty(localName = "MeldekortId")
    val meldekortId: Long
)