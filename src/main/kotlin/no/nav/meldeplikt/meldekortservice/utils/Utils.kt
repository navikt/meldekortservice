package no.nav.meldeplikt.meldekortservice.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException

internal const val BASE_PATH = "/meldekortservice"

internal const val API_PATH = "$BASE_PATH/api"
internal const val INTERNAL_PATH = "$BASE_PATH/internal"

internal const val MELDEKORT_PATH = "$API_PATH/meldekort"
internal const val PERSON_PATH = "$API_PATH/person"
internal const val WEBLOGIC_PING_PATH = "$API_PATH/weblogic"

internal const val KONTROLL_KONTROLL = "/api/kontroll"
internal const val KONTROLL_TOKENINFO = "/api/tokeninfo"

internal const val ARENA_ORDS_API_V1 = "/api/v1/meldeplikt"
internal const val ARENA_ORDS_TOKEN_PATH = "/api/oauth/token"
internal const val ARENA_ORDS_HENT_MELDEKORT = "$ARENA_ORDS_API_V1/meldekort?fnr="
internal const val ARENA_ORDS_HENT_HISTORISKE_MELDEKORT = "$ARENA_ORDS_API_V1/meldekort/historiske?fnr="
internal const val ARENA_ORDS_MELDEPERIODER_PARAM = "&antMeldeperioder="
internal const val ARENA_ORDS_HENT_MELDEKORTDETALJER = "$ARENA_ORDS_API_V1/meldekort/detaljer?meldekortId="
internal const val ARENA_ORDS_KOPIER_MELDEKORT = "$ARENA_ORDS_API_V1/meldekort/kopi"
internal const val ARENA_ORDS_ENDRE_MELDEFORM = "$ARENA_ORDS_API_V1/meldeform"

internal const val SBL_ARBEID_USERNAME = "srvSBLArbeid.username"
internal const val SBL_ARBEID_PASSWORD = "srvSBLArbeid.password"

internal const val DB_ORACLE_USERNAME = "oracleDbUser.username"
internal const val DB_ORACLE_PASSWORD = "oracleDbUser.password"
internal const val DB_ORACLE_CONF = "oracleDbConf.jdbcUrl"


const val vaultUrl = "https://vault.adeo.no"
const val vaultTokenPath = "/var/run/secrets/nais.io/vault/vault_token"

internal val HTTP_STATUS_CODES_2XX = IntRange(200, 299)

private val xmlMapper = XmlMapper()

internal data class ErrorMessage(val error: String)

internal class Error

internal suspend fun PipelineContext<Unit, ApplicationCall>.respondOrError(block: suspend() -> Any) =
    try {
        val res = block()
        call.respond(HttpStatusCode.OK, res)
    } catch (e: Exception) {
        if (e is NoContentException) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            defaultLog.error("Feil i meldekortservice", e)
            val eMsg = when (e) {
                is java.util.concurrent.TimeoutException -> "Arena ikke tilgjengelig"
                else -> if (e.localizedMessage != null) e.localizedMessage else "exception occurred"
            }
            if (e is SecurityException || eMsg.contains("400 Bad Request", true)) {
                call.respond(HttpStatusCode.BadRequest, ErrorMessage(eMsg))
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable, ErrorMessage(eMsg))
            }
        }
    }

fun isCurrentlyRunningOnNais(): Boolean {
    return System.getenv("NAIS_APP_NAME") != null
}

fun <T> mapFraXml(xml: String, responseKlasse: Class<T>): T {
    return xmlMapper.readValue(xml, responseKlasse)
}

val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .registerModule(ParameterNamesModule())
    .enable(SerializationFeature.INDENT_OUTPUT)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)