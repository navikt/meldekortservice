package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Meldekortdetaljer @JvmOverloads constructor (
    val id: String? = "",
    val personId: Long = 0,
    var fodselsnr: String = "",
    val meldekortId: Long = 0,
    val meldeperiode: String = "",
    var meldegruppe: String = "NULL",
    val arkivnokkel: String = "",
    val kortType: KortType,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val meldeDato: LocalDate? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val lestDato: LocalDate? = null,
    val sporsmal: Sporsmal? = null,
    val begrunnelse: String? = ""
)