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
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import no.nav.meldeplikt.meldekortservice.config.OutgoingCallLoggingPlugin
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.utils.swagger.Contact
import no.nav.meldeplikt.meldekortservice.utils.swagger.Information
import no.nav.meldeplikt.meldekortservice.utils.swagger.Swagger
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.slf4j.MDC
import java.net.ProxySelector
import java.util.*

internal const val BASE_PATH = "/meldekortservice"

internal const val API_PATH = "$BASE_PATH/api"
internal const val INTERNAL_PATH = "$BASE_PATH/internal"
internal const val SWAGGER_URL_V1 = "$INTERNAL_PATH/apidocs/index.html"

internal const val MELDEKORT_PATH = "$API_PATH/meldekort"
internal const val PERSON_PATH = "$API_PATH/person"
internal const val HISTORISKE_MELDEKORT_PATH = "$PERSON_PATH/historiskemeldekort"
internal const val PERSON_MELDEKORT_PATH = "$PERSON_PATH/meldekort"
internal const val SKRIVEMODUS_PATH = "$API_PATH/skrivemodus"

internal const val ARENA_ORDS_API_V1_APP = "/api/v1/app"
internal const val ARENA_ORDS_HENT_SKRIVEMODUS = "$ARENA_ORDS_API_V1_APP/skrivemodus"
internal const val ARENA_ORDS_API_V2_MELDEPLIKT = "/api/v2/meldeplikt"
internal const val ARENA_ORDS_API_V3_MELDEPLIKT = "/api/v3/meldeplikt"
internal const val ARENA_ORDS_TOKEN_PATH = "/api/oauth/token"
internal const val ARENA_ORDS_HENT_MELDEKORT = "$ARENA_ORDS_API_V2_MELDEPLIKT/meldekort"
internal const val ARENA_ORDS_HENT_HISTORISKE_MELDEKORT = "$ARENA_ORDS_API_V2_MELDEPLIKT/meldekort/historiske?"
internal const val ARENA_ORDS_MELDEPERIODER_PARAM = "antMeldeperioder="
internal const val ARENA_ORDS_HENT_MELDEKORTDETALJER = "$ARENA_ORDS_API_V2_MELDEPLIKT/meldekort/detaljer?meldekortId="
internal const val ARENA_ORDS_KOPIER_MELDEKORT = "$ARENA_ORDS_API_V2_MELDEPLIKT/meldekort/kopi"
internal const val ARENA_ORDS_HENT_MELDEGRUPPER = "$ARENA_ORDS_API_V3_MELDEPLIKT/kontroll/meldegruppe"

internal const val DB_ORACLE_USERNAME = "oracleDbUser.username"
internal const val DB_ORACLE_PASSWORD = "oracleDbUser.password"
internal const val DB_ORACLE_CONF = "oracleDbConf.jdbcUrl"

internal val HTTP_STATUS_CODES_2XX = IntRange(200, 299)

internal const val MDC_CORRELATION_ID = "correlationId"

internal data class ErrorMessage(val error: String)

internal class Error

val swagger = Swagger(
    info = Information(
        version = "1",
        title = "Meldekortservice",
        description = "Proxy-api for meldekort-applikasjonen (front-end). Api'et benyttes mot Arena  \n" +
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

internal suspend fun RoutingContext.respondOrError(block: suspend () -> Any) =
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
        OutgoingCallLoggingPlugin().intercept(this)
    }
    expectSuccess = false
}

lateinit var httpClient: HttpClient
fun defaultHttpClient(): HttpClient {
    if (!::httpClient.isInitialized) {
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

        if (!pid.isMissing) {
            return pid.asString()
        } else if (!sub.isMissing) {
            return sub.asString()
        }

        return "subject (ident) ikke funnet"
    } catch (exception: JWTDecodeException) {
        return "feil token"
    }
}
