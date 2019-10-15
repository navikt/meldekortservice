package no.nav.meldeplikt.meldekortservice.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Meldekortdetaljer @JvmOverloads constructor (
    val id: String? = "",
    val personId: Long = 0,
    val fodselsnr: String = "",
    val meldekortId: Long = 0,
    val meldeperiode: String = "",
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