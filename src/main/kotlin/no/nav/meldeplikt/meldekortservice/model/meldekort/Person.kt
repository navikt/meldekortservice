package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.google.gson.annotations.SerializedName

data class Person (
    @SerializedName("PersonId")
    val personId: Long,
    @SerializedName("Etternavn")
    val etternavn: String,
    @SerializedName("Fornavn")
    val fornavn: String,
    @SerializedName("Maalformkode")
    val maalformkode: String,
    @SerializedName("Meldeform")
    val meldeform: String,
    @SerializedName("MeldekortListe")
    val meldekortListe: List<Meldekort>,
    @SerializedName("AntallGjenstaaendeFeriedager")
    val antallGjenstaaendeFeriedager: Int = 0,
    @SerializedName("FravaerListe")
    val fravaerListe: List<FravaerType>?
)