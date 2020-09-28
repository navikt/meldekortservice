package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.aetat.arena.mk_meldekort.KontrollresultatType
import no.nav.meldeplikt.meldekortservice.model.Meldeperiode
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

data class KontrollResponse constructor (
    var meldekortId: Long = 0,
    var kortStatus: String,
    var arsakskoder: List<KontrollArsakskode>,
    var meldekortdager: List<KontrollMeldekortDag>

)