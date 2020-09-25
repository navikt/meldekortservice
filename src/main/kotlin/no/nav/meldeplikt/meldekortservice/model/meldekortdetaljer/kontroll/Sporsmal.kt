package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll

data class Sporsmal constructor(
    var arbeidssoker: Boolean? = null,
    var arbeidet: Boolean? = null,
    var syk: Boolean? = null,
    var annetFravaer: Boolean? = null,
    var kurs: Boolean? = null,
    var forskudd: Boolean? = null,
    var signatur: Boolean? = null
)