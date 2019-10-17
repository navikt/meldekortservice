package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class Meldekort (
    @SerializedName("MeldekortId")
    val meldekortId: Long,
    @SerializedName("KortType")
    val kortType: String,
    @SerializedName("Meldeperiode")
    val meldeperiode: String,
    @SerializedName("FraDato")
    val fraDato: LocalDate,
    @SerializedName("TilDato")
    val tilDato: LocalDate,
    @SerializedName("HoyesteMeldegruppe")
    val hoyesteMeldegruppe: String,
    @SerializedName("Beregningstatus")
    val beregningstatus: String,
    @SerializedName("Forskudd")
    val forskudd: Boolean,
    @SerializedName("MottatDato")
    val mottattDato: LocalDate?,
    @SerializedName("BruttoBelop")
    val bruttoBelop: String
)