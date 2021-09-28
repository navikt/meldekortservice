package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response

data class KontrollResponse constructor(
    var meldekortId: Long = 0,
    var kontrollStatus: String = "",
    var feilListe: List<KontrollFeil> = emptyList(),
    var oppfolgingListe: List<KontrollFeil> = emptyList()
)