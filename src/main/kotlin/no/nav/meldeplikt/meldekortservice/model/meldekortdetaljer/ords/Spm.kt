package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

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
    @JacksonXmlProperty(localName = "Signatur")
    val signatur: BooleanVerdi
)

data class Svar (
    @JacksonXmlProperty(localName = "SvarJa")
    val svarJa: BooleanVerdi,
    @JacksonXmlProperty(localName = "SvarNei")
    val svarNei: BooleanVerdi
)

data class BooleanVerdi (
    @JacksonXmlProperty(localName = "Verdi")
    val verdi: Boolean
)

