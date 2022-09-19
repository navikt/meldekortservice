package no.nav.meldeplikt.meldekortservice.model.database

import java.time.LocalDateTime

data class KallLogg(
    val korrelasjonId: String,
    val tidspunkt: LocalDateTime?,
    val type: String,
    val kallRetning: String,
    val method: String,
    val operation: String,
    val status: Int,
    val kallTid: Long,
    val request: String,
    val response: String?,
    val logginfo: String
)
