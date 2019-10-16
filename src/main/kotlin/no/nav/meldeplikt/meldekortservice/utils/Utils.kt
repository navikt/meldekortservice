package no.nav.meldeplikt.meldekortservice.utils

import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

internal const val BASE_PATH = "/meldekortservice"

internal const val API_PATH = "$BASE_PATH/api"
internal const val INTERNAL_PATH = "$BASE_PATH/internal"

internal const val MELDEKORT_PATH = "$API_PATH/meldekort"
internal const val PERSON_PATH = "$API_PATH/person"

internal const val ARENA_ORDS_TOKEN_PATH = "/meldeplikt/oauth/token"
internal const val ARENA_ORDS_HENT_MELDEKORT = "/meldeplikt/v1/meldekort/hent_meldekort/"

internal data class ErrorMessage(val error: String)

internal class Error

internal suspend fun PipelineContext<Unit, ApplicationCall>.respondOrServiceUnavailable(block: () -> Any) =
    try {
        val res = block()
        call.respond(res)
    } catch (e: Exception) {
        application.environment.log.error("Feil i meldekortservice", e)
        val eMsg = when (e) {
            is java.util.concurrent.TimeoutException -> "Arena ikke tilgjengelig"
            else -> if (e.localizedMessage != null) e.localizedMessage else "exception occurred"
        }
        call.respond(HttpStatusCode.ServiceUnavailable, ErrorMessage(eMsg))
    }

fun isCurrentlyRunningOnNais(): Boolean {
    return System.getenv("NAIS_APP_NAME") != null
}