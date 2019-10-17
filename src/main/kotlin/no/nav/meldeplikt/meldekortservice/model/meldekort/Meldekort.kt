package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class Meldekort (
    @JsonProperty("MeldekortId")
    val meldekortId: Long,
    @JsonProperty("KortType")
    val kortType: String,
    @JsonProperty("Meldeperiode")
    val meldeperiode: String,
    @JsonProperty("FraDato")
    val fraDato: LocalDate,
    @JsonProperty("TilDato")
    val tilDato: LocalDate,
    @JsonProperty("HoyesteMeldegruppe")
    val hoyesteMeldegruppe: String,
    @JsonProperty("Beregningstatus")
    val beregningstatus: String,
    @JsonProperty("Forskudd")
    val forskudd: Boolean,
    @JsonProperty("MottatDato")
    val mottattDato: LocalDate?,
    @JsonProperty("BruttoBelop")
    val bruttoBelop: String
)