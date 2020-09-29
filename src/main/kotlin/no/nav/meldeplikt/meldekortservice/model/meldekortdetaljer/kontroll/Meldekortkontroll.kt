package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.meldeplikt.meldekortservice.model.Meldeperiode
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

// Denne typen tilsvarer det som brukes i frontend og i meldekort-kontroll

data class Meldekortkontroll constructor (
    var meldekortId: Long = 0,
    var kortType: String,
    var kortStatus: String?,
    var meldegruppe: String,
    var meldeperiode: MeldeperiodeInn,
    var fravaersdager: List<FravaerInn>,
    var sporsmal: Sporsmal

)