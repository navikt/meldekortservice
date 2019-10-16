package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class FravaerType (
    @SerializedName("FraDato")
    val fraDato: LocalDate,
    @SerializedName("TilDato")
    val tilDato: LocalDate,
    @SerializedName("Type")
    val type: String
)