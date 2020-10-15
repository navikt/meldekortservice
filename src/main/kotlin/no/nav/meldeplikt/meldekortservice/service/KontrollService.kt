package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.KontrollServiceConfiguration
import no.nav.meldeplikt.meldekortservice.mapper.KontrollertTypeMapper
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import java.util.*


class KontrollService(
    private val config: KontrollServiceConfiguration
) {

    private val mapper = ObjectMapper()

    private val log = getLogger(KontrollService::class)
    private val env = Environment()
    private val responseMapper = KontrollertTypeMapper()

    private val azureGraphClientId: ClientId = ClientId("https://graph.microsoft.com")
    private val meldekortKontrollResource: Resource = Resource(
        ClientId(env.meldekortKontrollClientid),
        url = env.meldekortKontrollUrl
    )

    private val httpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }
    suspend fun kontroller(meldekort: Meldekortkontroll): MeldekortKontrollertType {
        val message = kontrollClient.post<KontrollResponse> {
            url("${env.meldekortKontrollUrl}$KONTROLL_KONTROLL")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer " + hentAadToken())
            body = meldekort
        }
        defaultLog.info(message.toString())

        return responseMapper.mapKontrollResponseToKontrollertType(message)
    }

    private val kontrollClient: HttpClient = HttpClient {
        engine {
            response.apply {
                charset(Charsets.UTF_8.displayName())
            }
        }
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }

    private val azureClient: HttpClient = HttpClient {
        engine {
            response.apply {
                charset(Charsets.UTF_8.displayName())
            }
        }
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }

    // TODO: Cache
    private suspend fun hentAadToken(): AccessToken {
        log.info("Henter token fra AAD")

        val ret = if (isCurrentlyRunningOnNais()) {
            getAccessTokenForResource(meldekortKontrollResource)
        } else {
            log.info("Henter ikke token da appen kjører lokalt")
            AccessToken("lokalt", 0, "Lokal")
        }
        defaultLog.info(ret.toString())
        return ret
    }

    // Service-to-service access token request (client credentials grant)
    private suspend fun getAccessTokenForResource(resource: Resource): AccessToken {
        val par = Parameters.build {
            append(Params.clientId, env.oauthClientId)
            append(Params.clientSecret, env.oauthClientSecret)
            append(Params.scope, resource.formatScopes())
            append(Params.grantType, GrantType.clientCredentials)
        }
        log.info("par: $par")
//        return submitForm(par)
        return postForm(resource)
    }

    private suspend inline fun submitForm(formParameters: Parameters): AccessToken {
        val u = config.azureAd.openIdConfiguration.tokenEndpoint
        log.info("AAD Url: $u")
        return httpClient.submitForm(
            url = u,

//            method = HttpMethod.Post,
            formParameters = formParameters
        )
    }

    // application/x-www-form-urlencoded
//
//    private inline fun urlEncode(par: Parameters): String {
//        val result = StringBuilder()
//        var first = true
//        for ((key, value) in params.entrySet()) {
//            if (first) first = false else result.append("&")
//            result.append(URLEncoder.encode(key, "UTF-8"))
//            result.append("=")
//            result.append(URLEncoder.encode(value, "UTF-8"))
//        }
//        return result.toString()
//    }
//
    private suspend inline fun postForm(resource: Resource): AccessToken {
        val u = config.azureAd.openIdConfiguration.tokenEndpoint
        log.info("AAD Url: $u")
        var p = Params.clientId+"="+env.oauthClientId
        p+="&"+Params.clientSecret+"="+env.oauthClientSecret
        p+="&"+Params.scope+"="+resource.formatScopes()
        p+="&"+Params.grantType+"="+GrantType.clientCredentials
        log.info("Body: $p")
        val b2 = TextContent(mapper.writeValueAsString(p), contentType = ContentType.Application.FormUrlEncoded)
        log.info("Body2: $b2")
        val message = azureClient.post<AccessToken> {
            url(u)
            contentType(ContentType.Application.FormUrlEncoded)
            body = b2

//            body = p
        }
        log.info("Token: $message")

        return message

    }

    internal object GrantType {
        const val clientCredentials = "client_credentials"
        const val jwt = "urn:ietf:params:oauth:grant-type:jwt-bearer"
    }


    data class AccessToken(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("expires_in")
        val expiresIn: Int,
        @JsonProperty("token_type")
        val tokenType: String
    )

//    data class ThrowableErrorMessage(
//        val message: String,
//        val throwable: Throwable
//    ) {
//        fun toErrorResponse() = ErrorResponse(message, throwable.toString())
//    }

//    data class ErrorResponse(
//        val message: String,
//        val cause: String
//    )

    internal object Params {
        const val assertion: String = "assertion"
        const val assertionType: String = "assertion_type"
        const val clientId: String = "client_id"
        const val clientSecret: String = "client_secret"
        const val grantType: String = "grant_type"
        const val requestedTokenUse: String = "requested_token_use"
        const val scope: String = "scope"
    }

    class Scope(private val scope: String) {
        override fun toString(): String = scope
    }

    class ClientId(private val clientId: String) {
        fun defaultScope(): Scope = Scope("${this.withApiPrefix()}/.default")
        fun withApiPrefix(): Scope = Scope("api://$clientId")
        override fun toString(): String = clientId
    }

    data class Resource(
        val clientId: ClientId,
        val scopes: Set<Scope> = emptySet(),
        val url: String,
        val response: Any? = null
    ) {
        fun formatScopes(): String = when {
            scopes.isEmpty() -> clientId.defaultScope().toString()
            else -> scopes.mapToString(clientId)
        }

    fun Set<Scope>.mapToString(clientId: ClientId): String = this.joinToString(separator = " ") { scope ->
        val azureGraphClientId = ClientId("https://graph.microsoft.com")

        when (clientId) {
            azureGraphClientId -> "$scope"
            else -> "${clientId.withApiPrefix()}/$scope"
        }
    }

        fun addResponse(response: Any?): Resource = this.copy(response = response)
    }

    internal fun getAzureGraphResource(query: String) = Resource(
        clientId = azureGraphClientId,
        url = "https://graph.microsoft.com/v1.0/me?\$select=$query",
        scopes = setOf(Scope("https://graph.microsoft.com/.default"))
    )

    // Denne bør være OK?
    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.ordsClientId}:${env.ordsClientSecret}"
        headers.append("Authorization", "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}")
    }
}