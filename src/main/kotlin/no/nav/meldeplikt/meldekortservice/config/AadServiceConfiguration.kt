package no.nav.meldeplikt.meldekortservice.config

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.utils.defaultHttpClient
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais

data class AadServiceConfiguration(
    val azureAd: AzureAd = AzureAd()
) {
    data class AzureAd(
        val clientId: String = Environment().oauthClientId,
        val clientSecret: String = Environment().oauthClientSecret,
        val tenant: String = Environment().oauthTenant,
        val authorityEndpoint: String = Environment().oauthEndpoint.removeSuffix("/"),
        val openIdConfiguration: AzureAdOpenIdConfiguration = if (isCurrentlyRunningOnNais()) {
            runBlocking {
                defaultHttpClient().get("$authorityEndpoint/$tenant/v2.0/.well-known/openid-configuration").body()
            }
        } else {
            AzureAdOpenIdConfiguration("test", "test", "test", "test")
        }
    )
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
