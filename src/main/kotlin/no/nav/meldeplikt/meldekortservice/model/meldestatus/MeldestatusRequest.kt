package no.nav.meldeplikt.meldekortservice.model.meldestatus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MeldestatusRequest(
    val arenaPersonId: Long? = null,
    val personident: String? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val sokeDato: LocalDate? = null
)
