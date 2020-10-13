package no.nav.meldeplikt.meldekortservice.aadclient

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.readText
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(AzureAdClient::class.java)

class AzureAdClient(
    private val config: Configuration.AzureAd,
    private val httpClient: HttpClient = defaultHttpClient
) {

    private suspend inline fun submitForm(formParameters: Parameters): Result<AccessToken, ThrowableErrorMessage> =
        runCatching {
            httpClient.submitForm<AccessToken>(
                url = config.openIdConfiguration.tokenEndpoint,
                formParameters = formParameters
            )
        }.fold(
            onSuccess = { result -> Ok(result) },
            onFailure = { error -> error.handleError("Could not fetch access token from authority endpoint") }
        )

    private suspend inline fun get(resource: Resource, oboAccessToken: AccessToken): Result<JsonNode, ThrowableErrorMessage> =
        runCatching {
            httpClient.get<JsonNode>(resource.url) {
                header(HttpHeaders.Authorization, "Bearer ${oboAccessToken.accessToken}")
            }
        }.fold(
            onSuccess = { result -> Ok(result) },
            onFailure = { error -> error.handleError("Could not fetch user info from Graph API") }
        )

    private suspend fun Throwable.handleError(message: String): Err<ThrowableErrorMessage> {
        val responseBody: String? = when (this) {
            is ResponseException -> this.response?.readText()
            else -> null
        }
        return "$message. response body: $responseBody"
            .also { errorMessage -> logger.error(errorMessage, this) }
            .let { errorMessage -> Err(ThrowableErrorMessage(errorMessage, this)) }
    }

    // Service-to-service access token request (client credentials grant)
    suspend fun getAccessTokenForResource(resource: Resource): Result<AccessToken, ThrowableErrorMessage> =
        submitForm(
            Parameters.build {
                append(Params.clientId, config.clientId)
                append(Params.clientSecret, config.clientSecret)
                append(Params.scope, resource.formatScopes())
                append(Params.grantType, GrantType.clientCredentials)
            }
        )

    // Service-to-service access token request (on-behalf-of flow)
    suspend fun getAccessTokenForResourceOnBehalfOf(resource: Resource, accessToken: String): Result<AccessToken, ThrowableErrorMessage> =
        submitForm(
            Parameters.build {
                append(Params.clientId, config.clientId)
                append(Params.clientSecret, config.clientSecret)
                append(Params.scope, resource.formatScopes())
                append(Params.grantType, GrantType.jwt)
                append(Params.requestedTokenUse, "on_behalf_of")
                append(Params.assertion, accessToken)
                append(Params.assertionType, AssertionType.jwt)
            }
        )

    // Graph API lookup (on-behalf-of flow)
    suspend fun getUserInfoFromGraph(accessToken: String): Result<JsonNode, ThrowableErrorMessage> {
        val queryProperties = "onPremisesSamAccountName,displayName,givenName,mail,officeLocation,surname,userPrincipalName,id,jobTitle"
        val resource: Resource = getAzureGraphResource(queryProperties)
        return getAccessTokenForResourceOnBehalfOf(resource, accessToken)
            .andThen { oboAccessToken -> get(resource, oboAccessToken) }
    }
}

data class AccessToken(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("token_type")
    val tokenType: String
)

internal object GrantType {
    const val clientCredentials = "client_credentials"
    const val jwt = "urn:ietf:params:oauth:grant-type:jwt-bearer"
}

internal object AssertionType {
    const val jwt = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
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
