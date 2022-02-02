package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

/**
 * Denne typen er basert på det som brukes i frontend og i meldekort-kontroll
 */
data class Meldekortkontroll constructor(
    var meldekortId: Long = 0,
    var fnr: String,
    var personId: Long = 0,
    var kilde: String,
    var kortType: String,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val meldedato: LocalDate? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val periodeFra: LocalDate? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val periodeTil: LocalDate? = null,
    var meldegruppe: String,
    var annetFravaer: Boolean? = null,
    var arbeidet: Boolean? = null,
    var arbeidssoker: Boolean? = null,
    var kurs: Boolean? = null,
    var syk: Boolean? = null,
    var begrunnelse: String?,
    var meldekortdager: List<FravaerInn>

)