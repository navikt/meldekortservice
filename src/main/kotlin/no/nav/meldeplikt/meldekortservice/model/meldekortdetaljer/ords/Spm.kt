package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords

data class Spm (
    val arbeidssoker: Svar,
    val arbeidet: Svar,
    val syk: Svar,
    val annetFravaer: Svar,
    val kurs: Svar,
    val forskudd: BooleanVerdi,
    val signatur: BooleanVerdi
)

data class Svar (
    val svarJa: BooleanVerdi,
    val svarNei: BooleanVerdi
)

data class BooleanVerdi (
    val verdi: Boolean
)

