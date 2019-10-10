package no.nav.meldeplikt.meldekortservice.model

data class Sporsmal @JvmOverloads constructor (
    val arbeidssoker: Boolean? = null,
    val arbeidet: Boolean? = null,
    val syk: Boolean? = null,
    val annetFravaer: Boolean? = null,
    val kurs: Boolean? = null,
    val forskudd: Boolean? = null,
    val signatur: Boolean? = null,
    val meldekortDager: List<MeldekortDag>? = null
)