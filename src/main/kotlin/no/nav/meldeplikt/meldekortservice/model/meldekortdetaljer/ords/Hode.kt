package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords

import java.time.LocalDate

data class Hode (
    val personId: LongVerdi,
    val fodselsnr: StringVerdi,
    val meldekortId: LongVerdi,
    val meldeperiode: String,
    val arkivnokkel: String,
    val kortType: String,
    val meldeDato: LocalDate? = null,
    val lestDato: LocalDate? = null
)

data class LongVerdi (
    val verdi: Long
)

data class StringVerdi (
    val verdi: String
)