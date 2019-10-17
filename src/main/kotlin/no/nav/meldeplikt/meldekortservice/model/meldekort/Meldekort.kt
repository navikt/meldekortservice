package no.nav.meldeplikt.meldekortservice.model.meldekort

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

data class Meldekort (
    @JsonProperty("MeldekortId")
    val meldekortId: Long,
    @JsonProperty("KortType")
    val kortType: String,
    @JsonProperty("Meldeperiode")
    val meldeperiode: String,
    @JsonProperty("FraDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val fraDato: LocalDate,
    @JsonProperty("TilDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val tilDato: LocalDate,
    @JsonProperty("HoyesteMeldegruppe")
    val hoyesteMeldegruppe: String,
    @JsonProperty("Beregningstatus")
    val beregningstatus: String,
    @JsonProperty("Forskudd")
    val forskudd: Boolean,
    @JsonProperty("MottatDato")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val mottattDato: LocalDate?,
    @JsonProperty("BruttoBelop")
    val bruttoBelop: String?
)