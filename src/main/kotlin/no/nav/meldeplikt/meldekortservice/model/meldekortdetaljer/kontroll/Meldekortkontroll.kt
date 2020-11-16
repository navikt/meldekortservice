package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll

/**
 * Denne typen tilsvarer det som brukes i frontend og i meldekort-kontroll
 */
data class Meldekortkontroll constructor(
    var meldekortId: Long = 0,
    var kortType: String,
    var kortStatus: String?,
    var meldegruppe: String,
    var meldeperiode: MeldeperiodeInn,
    var fravaersdager: List<FravaerInn>,
    var sporsmal: Sporsmal,
    var begrunnelse: String?

)