package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.BooleanVerdi
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.StringVerdi

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MeldekortDag (
    @JacksonXmlProperty(localName = "Dag")
    val dag: Int,
    @JacksonXmlProperty(localName = "ArbeidetTimerSum")
    val arbeidetTimerSum: StringVerdi,
    @JacksonXmlProperty(localName = "Syk")
    val syk: BooleanVerdi,
    @JacksonXmlProperty(localName = "AnnetFravaer")
    val annetFravaer: BooleanVerdi,
    @JacksonXmlProperty(localName = "Kurs")
    val kurs: BooleanVerdi
)