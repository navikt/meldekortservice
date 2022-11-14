package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response

data class KontrollFeil constructor(
    var kode: String? = null,
    var tekst: String? = null,
    var dag: Int? = null,
    var params: Array<String>? = null
)