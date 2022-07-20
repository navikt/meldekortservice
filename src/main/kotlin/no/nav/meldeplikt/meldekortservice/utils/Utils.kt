package no.nav.meldeplikt.meldekortservice.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException

internal const val BASE_PATH = "/meldekortservice"

internal const val API_PATH = "$BASE_PATH/api"
internal const val INTERNAL_PATH = "$BASE_PATH/internal"

internal const val MELDEKORT_PATH = "$API_PATH/meldekort"
internal const val PERSON_PATH = "$API_PATH/person"
internal const val WEBLOGIC_PING_PATH = "$API_PATH/weblogic"

internal const val KONTROLL_KONTROLL = "/api/v1/kontroll"

internal const val ARENA_ORDS_API_V1 = "/api/v1/meldeplikt"
internal const val ARENA_ORDS_TOKEN_PATH = "/api/oauth/token"
internal const val ARENA_ORDS_HENT_MELDEKORT = "$ARENA_ORDS_API_V1/meldekort?fnr="
internal const val ARENA_ORDS_HENT_HISTORISKE_MELDEKORT = "$ARENA_ORDS_API_V1/meldekort/historiske?fnr="
internal const val ARENA_ORDS_MELDEPERIODER_PARAM = "&antMeldeperioder="
internal const val ARENA_ORDS_HENT_MELDEKORTDETALJER = "$ARENA_ORDS_API_V1/meldekort/detaljer?meldekortId="
internal const val ARENA_ORDS_KOPIER_MELDEKORT = "$ARENA_ORDS_API_V1/meldekort/kopi"

internal const val SBL_ARBEID_USERNAME = "srvSBLArbeid.username"
internal const val SBL_ARBEID_PASSWORD = "srvSBLArbeid.password"

internal const val DB_ORACLE_USERNAME = "oracleDbUser.username"
internal const val DB_ORACLE_PASSWORD = "oracleDbUser.password"
internal const val DB_ORACLE_CONF = "oracleDbConf.jdbcUrl"

internal const val STS_PATH = "/rest/v1/sts/token"

internal const val JOURNALPOSTAPI_PATH = "/rest/journalpostapi/v1"
internal const val JOURNALPOST_PATH = "$JOURNALPOSTAPI_PATH/journalpost"

internal const val SOAP_STS_URL_KEY = "no.nav.modig.security.sts.url"
internal const val SOAP_SYSTEMUSER_USERNAME = "no.nav.modig.security.systemuser.username"
internal const val SOAP_SYSTEMUSER_PASSWORD = "no.nav.modig.security.systemuser.password"

internal val HTTP_STATUS_CODES_2XX = IntRange(200, 299)

internal data class ErrorMessage(val error: String)

internal class Error

internal suspend fun PipelineContext<Unit, ApplicationCall>.respondOrError(block: suspend () -> Any) =
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
    return XmlMapper().readValue(xml, responseKlasse)
}

val defaultXmlMapper: ObjectMapper = XmlMapper().registerModule(
    KotlinModule.Builder()
        .withReflectionCacheSize(512)
        .configure(KotlinFeature.NullToEmptyCollection, false)
        .configure(KotlinFeature.NullToEmptyMap, false)
        .configure(KotlinFeature.NullIsSameAsDefault, false)
        .configure(KotlinFeature.SingletonSupport, false)
        .configure(KotlinFeature.StrictNullChecks, false)
        .build()
)

val defaultObjectMapper = jacksonObjectMapper()

val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .registerModule(ParameterNamesModule())
    .enable(SerializationFeature.INDENT_OUTPUT)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)