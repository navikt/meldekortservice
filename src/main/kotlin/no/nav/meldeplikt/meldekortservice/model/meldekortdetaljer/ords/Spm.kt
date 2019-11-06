package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.verdi.BooleanVerdi

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Spm (
    @JacksonXmlProperty(localName = "Arbeidssoker")
    val arbeidssoker: Svar,
    @JacksonXmlProperty(localName = "Arbeidet")
    val arbeidet: Svar,
    @JacksonXmlProperty(localName = "Syk")
    val syk: Svar,
    @JacksonXmlProperty(localName = "AnnetFravaer")
    val annetFravaer: Svar,
    @JacksonXmlProperty(localName = "Kurs")
    val kurs: Svar,
    @JacksonXmlProperty(localName = "Forskudd")
    val forskudd: BooleanVerdi,
    @JacksonXmlProperty(localName = "MeldekortDager")
    val meldekortDager: List<MeldekortDag>? = null,
    @JacksonXmlProperty(localName = "Signatur")
    val signatur: BooleanVerdi
)