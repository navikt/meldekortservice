package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.cache
import no.nav.meldeplikt.meldekortservice.mapper.KontrollertTypeMapper
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.utils.*
import java.util.*

class KontrollService {

    private val log = getLogger(KontrollService::class)
    private val env = Environment()
    private val responseMapper = KontrollertTypeMapper()

    private val azureGraphClientId: ClientId = ClientId("https://graph.microsoft.com")
    private val meldekortKontrollResource: Resource = Resource(ClientId(env.meldekortKontrollClientid), url = env.meldekortKontrollUrl)

    private val httpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }
//    private val httpClient: HttpClient = client

    suspend fun kontroller(meldekort: Meldekortkontroll): MeldekortKontrollertType {
        val message = kontrollClient.post<KontrollResponse> {
            url("${env.meldekortKontrollUrl}$KONTROLL_KONTROLL")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer "+hentAadToken())
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

    // TODO: Cache
    private suspend fun hentAadToken(): AccessToken {
//        log.info("Cache timet ut. Henter token")

        return if (isCurrentlyRunningOnNais()) {
            getAccessTokenForResource(meldekortKontrollResource)
        } else {
            log.info("Henter ikke token da appen kjører lokalt")
            AccessToken("lokalt", 0, "Lokal")
        }
    }

    // Service-to-service access token request (client credentials grant)
    suspend fun getAccessTokenForResource(resource: Resource): AccessToken =
        submitForm(
            Parameters.build {
                append(Params.clientId, env.oauthClientId)
                append(Params.clientSecret, env.oauthClientSecret)
                append(Params.scope, resource.formatScopes())
                append(Params.grantType, GrantType.clientCredentials)
            }
        )

    private suspend inline fun submitForm(formParameters: Parameters): AccessToken =
        httpClient.submitForm(
            url = "https://login.microsoftonline.com",
            formParameters = formParameters
        )

//    data class AzureAd(
//        val clientId: String = "xxx",
//        val clientSecret: String = "yyy",
//        val tenant: String = "zzz",
//        val authorityEndpoint: String = "oauthserver".removeSuffix("/"),
//        val openIdConfiguration: AzureAdOpenIdConfiguration = runBlocking {
//            httpClient.get<AzureAdOpenIdConfiguration>("$authorityEndpoint/$tenant/v2.0/.well-known/openid-configuration")
//        }
//    )

    internal object GrantType {
        const val clientCredentials = "client_credentials"
        const val jwt = "urn:ietf:params:oauth:grant-type:jwt-bearer"
    }

    // Lese fra env
    data class AzureAdOpenIdConfiguration(
        @JsonProperty("jwks_uri")
        val jwksUri: String,
        @JsonProperty("issuer")
        val issuer: String,
        @JsonProperty("token_endpoint")
        val tokenEndpoint: String,
        @JsonProperty("authorization_endpoint")
        val authorizationEndpoint: String
    )

    data class AccessToken(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("expires_in")
        val expiresIn: Int,
        @JsonProperty("token_type")
        val tokenType: String
    )

    data class ThrowableErrorMessage(
        val message: String,
        val throwable: Throwable
    ) {
        fun toErrorResponse() = ErrorResponse(message, throwable.toString())
    }

    data class ErrorResponse(
        val message: String,
        val cause: String
    )

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