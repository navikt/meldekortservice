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
    val meldekortId: Long = 0,
    val kortType: String,
    val kortStatus: String,
    val meldegruppe: String,
    val meldeperiode: MeldeperiodeInn,
    val fravaersdager: List<FravaerInn>,
    val sporsmal: Sporsmal

)