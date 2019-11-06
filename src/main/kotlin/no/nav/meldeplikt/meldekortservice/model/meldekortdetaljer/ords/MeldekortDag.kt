package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.verdi.BooleanVerdi
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.verdi.IntVerdi

data class MeldekortDag (
    @JacksonXmlProperty(localName = "Dag")
    val dag: Int,
    @JacksonXmlProperty(localName = "ArbeidetTimerSum")
    val arbeidetTimerSum: IntVerdi,
    @JacksonXmlProperty(localName = "Syk")
    val syk: BooleanVerdi,
    @JacksonXmlProperty(localName = "AnnetFravaer")
    val annetFravaer: BooleanVerdi,
    @JacksonXmlProperty(localName = "Kurs")
    val kurs: BooleanVerdi
)