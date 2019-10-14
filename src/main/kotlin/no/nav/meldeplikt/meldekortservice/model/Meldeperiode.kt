package no.nav.meldeplikt.meldekortservice.model

import java.time.LocalDate

data class Meldeperiode (
    val id: String?,
    val meldeperiodeNavn: String,
    val fraDato: LocalDate,
    val tilDato: LocalDate
)