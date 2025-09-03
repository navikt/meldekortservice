package no.nav.meldeplikt.meldekortservice.model.meldestatus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.meldeplikt.meldekortservice.utils.LocalDateTimeDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateTimeSerializer
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MeldestatusResponse(
    val arenaPersonId: Long,
    val personIdent: String,
    val formidlingsgruppe: String,
    val harMeldtSeg: Boolean,
    val meldepliktListe: List<Meldeplikt>? = null,
    val meldegruppeListe: List<Meldegruppe>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Meldeplikt(
    val meldeplikt: Boolean,
    val meldepliktperiode: Periode? = null,
    val begrunnelse: String? = null,
    val stemplingsdata: Endring
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Meldegruppe(
    val meldegruppe: String,
    val meldegruppeperiode: Periode? = null,
    val begrunnelse: String? = null,
    val stemplingsdata: Endring
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Periode(
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val fom: LocalDateTime,
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val tom: LocalDateTime? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Endring(
    val registrertAv: String,
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val registreringsdato: LocalDateTime,
    val endretAv: String,
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val endringsdato: LocalDateTime,
)
