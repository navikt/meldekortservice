package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import no.nav.meldeplikt.meldekortservice.config.AadServiceConfiguration
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.utils.getLogger
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

class AadService (
    private val config: AadServiceConfiguration
){
    private val log = getLogger(KontrollService::class)
    private val env = Environment()

    private val meldekortKontrollResource: Resource = Resource(
        ClientId(env.meldekortKontrollClientid),
        url = env.meldekortKontrollUrl
    )

    private val aadClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
        }
        engine {
            customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
        }
    }

    // TODO: Cache
    suspend fun hentAadToken(): AccessToken {
        return if (isCurrentlyRunningOnNais()) {
            log.info("Henter nytt token fra AAD")
            getAccessTokenForResource(meldekortKontrollResource)
        } else {
            log.info("Henter ikke token da appen kjører lokalt")
            AccessToken("lokalt", 0, "Lokal")
        }
    }

    // Service-to-service access token request (client credentials grant)
    private suspend fun getAccessTokenForResource(resource: Resource): AccessToken {
        return submitForm(Parameters.build {
            append(Params.clientId, env.oauthClientId)
            append(Params.clientSecret, env.oauthClientSecret)
            append(Params.scope, resource.formatScopes())
            append(Params.grantType, GrantType.clientCredentials)
        })
    }

    private suspend inline fun submitForm(formParameters: Parameters): AccessToken {
        return aadClient.submitForm(
            url = config.azureAd.openIdConfiguration.tokenEndpoint,
            formParameters = formParameters
        )
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

}
