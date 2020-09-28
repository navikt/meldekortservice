package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response

data class KontrollMeldekortDag constructor(
    var dag: Int? = null,
    var arbeidetTimerSum: Float = 0F,
    var syk: Boolean = false,
    var annetFravaer: Boolean = false,
    var kurs: Boolean = false,
    var meldegruppe: String? = null
)