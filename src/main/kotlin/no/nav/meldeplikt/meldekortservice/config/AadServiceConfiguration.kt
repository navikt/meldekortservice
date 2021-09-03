package no.nav.meldeplikt.meldekortservice.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

data class AadServiceConfiguration(
    val azureAd: AzureAd = AzureAd()
) {
    data class AzureAd(
        val clientId: String = Environment().oauthClientId,
        val clientSecret: String = Environment().oauthClientSecret,
        val tenant: String = Environment().oauthTenant,
        val authorityEndpoint: String = Environment().oauthEndpoint.removeSuffix("/"),
        val openIdConfiguration: AzureAdOpenIdConfiguration = runBlocking {
            defaultHttpClient.get("$authorityEndpoint/$tenant/v2.0/.well-known/openid-configuration")
        }
    )
}

internal val defaultHttpClient = HttpClient(Apache) {
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
