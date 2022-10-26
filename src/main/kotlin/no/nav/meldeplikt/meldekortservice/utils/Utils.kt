package no.nav.meldeplikt.meldekortservice.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import no.nav.cache.Cache
import no.nav.cache.CacheConfig
import no.nav.cache.CacheUtils
import no.nav.meldeplikt.meldekortservice.config.OutgoingCallLoggingPlugin
import no.nav.meldeplikt.meldekortservice.config.defaultDbService
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.utils.swagger.Contact
import no.nav.meldeplikt.meldekortservice.utils.swagger.Information
import no.nav.meldeplikt.meldekortservice.utils.swagger.Swagger
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.slf4j.MDC
import java.net.ProxySelector
import java.util.*

internal const val CACHE_ANTALL_MINUTTER = 55

// Årsaken til å multiplisere med 2 er at cache-implementasjonen dividerer timeout-verdien med 2...
internal const val CACHE_TIMEOUT: Long = CACHE_ANTALL_MINUTTER.toLong() * 60 * 1000 * 2
internal var CACHE: Cache<String, AccessToken> =
    CacheUtils.buildCache(CacheConfig.DEFAULT.withTimeToLiveMillis(CACHE_TIMEOUT))

internal const val BASE_PATH = "/meldekortservice"

internal const val API_PATH = "$BASE_PATH/api"
internal const val INTERNAL_PATH = "$BASE_PATH/internal"
internal const val SWAGGER_URL_V1 = "$INTERNAL_PATH/apidocs/index.html"

internal const val MELDEKORT_PATH = "$API_PATH/meldekort"
internal const val PERSON_PATH = "$API_PATH/person"
internal const val HISTORISKE_MELDEKORT_PATH = "$PERSON_PATH/historiskemeldekort"
internal const val PERSON_MELDEKORT_PATH = "$PERSON_PATH/meldekort"
internal const val OPPRETT_JOURNALPOST_PATH = "$PERSON_PATH/opprettJournalpost"
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

internal const val MDC_CORRELATION_ID = "correlationId"

internal data class ErrorMessage(val error: String)

internal class Error

@KtorExperimentalLocationsAPI
val swagger = Swagger(
    info = Information(
        version = "1",
        title = "Meldekortservice",
        description = "Proxy-api for meldekort-applikasjonen (front-end). Api'et benyttes mot Arena og meldekortkontroll-api  \n" +
                "GitHub repo: [https://github.com/navikt/meldekortservice](https://github.com/navikt/meldekortservice)  \n" +
                "Slack: [#team-meldeplikt](https://nav-it.slack.com/archives/CQ61EHWP9)",
        contact = Contact(
            email = "meldeplikt@nav.no"
        )
    )
)

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

val defaultObjectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .registerModule(ParameterNamesModule())
    .enable(SerializationFeature.INDENT_OUTPUT)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

fun headersToString(headers: List<String>): String {
    if (headers.size == 1) {
        return headers[0]
    }

    return headers.joinToString(",", "[", "]")
}

fun HttpClientConfig<*>.defaultHttpClientConfig() {
    install(ContentNegotiation) {
        register(
            ContentType.Application.Json,
            JacksonConverter(
                defaultObjectMapper
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            )
        )
    }
    install(HttpTimeout) {
        // max time periods
        connectTimeoutMillis = 5000 // required to establish a connection with a server
        requestTimeoutMillis = 10000 // required for an HTTP call: from sending a request to receiving a response
        socketTimeoutMillis = 10000 //  of inactivity between two data packets when exchanging data with a server
    }
    install("OutgoingCallInterceptor") {
        OutgoingCallLoggingPlugin(defaultDbService).intercept(this)
    }
    expectSuccess = false
}

lateinit var httpClient: HttpClient
fun defaultHttpClient(): HttpClient {
    if(!::httpClient.isInitialized) {
        httpClient = HttpClient(Apache) {
            defaultHttpClientConfig()
            engine {
                customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
            }
        }
    }

    return httpClient
}

fun generateCallId(): String {
    return "meldekortservice-${UUID.randomUUID()}"
}

fun getCallId(): String {
    var korrelasjonId = MDC.get(MDC_CORRELATION_ID)

    if (korrelasjonId == null || korrelasjonId.isBlank()) {
        korrelasjonId = generateCallId()
        MDC.put(MDC_CORRELATION_ID, korrelasjonId)
    }

    // DB has max 54 signs in the korrelasjon_id field, so we must not have more otherwise we will get SQL error
    if (korrelasjonId.length > 54) {
        korrelasjonId = korrelasjonId.substring(0, 54)
    }

    return korrelasjonId
}

fun extractSubject(authToken: String?): String {
    if (authToken == null) {
        return "token er null"
    }

    try {
        val jwt = JWT.decode(authToken)

        val pid = jwt.getClaim("pid")
        val sub = jwt.getClaim("sub")

        if (!pid.isNull) {
            return pid.asString()
        } else if (!sub.isNull) {
            return sub.asString()
        }

        return "subject (ident) ikke funnet"
    } catch (exception: JWTDecodeException) {
        return "feil token"
    }
}
