package no.nav.meldeplikt.meldekortservice.model

import java.util.*

data class Meldeperiode (
    val id: String?,
    val meldeperiodeNavn: String,
    val fraDato: Date,
    val tilDato: Date
)