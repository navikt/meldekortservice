package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import no.nav.meldeplikt.meldekortservice.config.AadServiceConfiguration
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector
import java.time.LocalDateTime

class AadService(
    private val config: AadServiceConfiguration
) {
    private val env = Environment()
    private val cacheSafetyMarginSeconds = 100
    private var aadToken: String = ""
    private var aadTokenExpires: LocalDateTime = LocalDateTime.now()

    private val meldekortKontrollResource: Resource = Resource(
        ClientId(env.meldekortKontrollClientid),
        url = env.meldekortKontrollUrl
    )

    private val aadClient = HttpClient(Apache) {
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
        engine {
            customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
        }
    }

    /**
     * Returnerer forrige hentede token hvis det fortsatt er gyldig, ellers hentes nytt token
     * som returneres.
     */
    suspend fun fetchAadToken(): String {
        if (aadTokenExpires.isBefore(LocalDateTime.now())) {
            if (isCurrentlyRunningOnNais()) {
                defaultLog.debug("Henter nytt token fra AAD")
                val token = getAccessTokenForResource(meldekortKontrollResource)
                aadToken = token.accessToken!!
                aadTokenExpires = LocalDateTime.now().plusSeconds((token.expiresIn?.toLong() ?: 0) - cacheSafetyMarginSeconds)
            } else {
                defaultLog.info("Henter ikke token da appen kjører lokalt")
                aadToken = "Lokalt"
                aadTokenExpires = LocalDateTime.now().plusYears(1L)
            }
        }
        return aadToken
    }

    // Service-to-service access token request (client credentials grant)
    private suspend fun getAccessTokenForResource(resource: Resource): AccessToken {
        return submitForm(Parameters.build {
            append(Params.clientId, env.oauthClientId)
            append(Params.clientSecret, env.oauthClientSecret)
            append(Params.scope, "api://" + resource.clientId + "/.default")
            append(Params.grantType, GrantType.clientCredentials)
        })
    }

    private suspend inline fun submitForm(formParameters: Parameters): AccessToken {
        return aadClient.submitForm(
            url = config.azureAd.openIdConfiguration.tokenEndpoint,
            formParameters = formParameters
        ).body()
    }

    internal object GrantType {
        const val clientCredentials = "client_credentials"
        const val jwt = "urn:ietf:params:oauth:grant-type:jwt-bearer"
    }

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
