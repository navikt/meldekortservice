package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MeldekortDag @JvmOverloads constructor (
    val dag: Int = 0,
    val arbeidetTimerSum: Float? = null,
    val syk: Boolean? = null,
    val annetFravaer: Boolean? = null,
    val kurs: Boolean? = null,
    val meldegruppe: String? = null
)