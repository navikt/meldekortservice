package no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import java.time.LocalDate

data class FravaerInn constructor(
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val dato: LocalDate? = null,
    val syk: Boolean? = null,
    val kurs: Boolean? = null,
    val annetFravaer: Boolean? = null,
    val arbeidTimer: Double? = null
)