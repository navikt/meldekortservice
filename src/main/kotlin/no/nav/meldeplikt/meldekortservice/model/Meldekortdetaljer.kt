package no.nav.meldeplikt.meldekortservice.model

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Meldekortdetaljer @JvmOverloads constructor (
    val id: String? = "",
    val personId: Long = 0,
    val fodselsnr: String = "",
    val meldekortId: Long = 0,
    val meldeperiode: String = "",
    val arkivnokkel: String = "",
    val kortType: KortType,
    val meldeDato: Date? = null,
    val lestDato: Date? = null,
    val sporsmal: Sporsmal? = null,
    val begrunnelse: String? = ""
)