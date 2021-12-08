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
    var personId: Long = 0,
    var fnr: String,
    var kilde: String,
    var kortType: String,
    var kortStatus: String?,
    var meldegruppe: String,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val periodeFra: LocalDate? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val periodeTil: LocalDate? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val kortKanSendesFra: LocalDate? = null,
    val kanKortSendes: Boolean? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val meldedato: LocalDate? = null,
    val periodeKode: String? = null,
    var fravaersdager: List<FravaerInn>,
    var arbeidssoker: Boolean? = null,
    var arbeidet: Boolean? = null,
    var syk: Boolean? = null,
    var annetFravaer: Boolean? = null,
    var kurs: Boolean? = null,
    var forskudd: Boolean? = null,
    var signatur: Boolean? = null,
    var begrunnelse: String?

)