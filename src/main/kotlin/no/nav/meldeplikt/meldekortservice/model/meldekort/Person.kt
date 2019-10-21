package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.fasterxml.jackson.annotation.JsonProperty

data class Person (
    //@JsonProperty("PersonId")
    val personId: Long,
    //@JsonProperty("Etternavn")
    val etternavn: String,
    //@JsonProperty("Fornavn")
    val fornavn: String,
    //@JsonProperty("Maalformkode")
    val maalformkode: String,
    //@JsonProperty("Meldeform")
    val meldeform: String,
    //@JsonProperty("MeldekortListe")
    val meldekortListe: List<Meldekort>,
    //@JsonProperty("AntallGjenstaaendeFeriedager")
    val antallGjenstaaendeFeriedager: Int? = 0,
    //@JsonProperty("FravaerListe")
    val fravaerListe: List<FravaerType>? = null
)