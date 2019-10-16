package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.cache
import no.nav.meldeplikt.meldekortservice.config.client
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.utils.ARENA_ORDS_TOKEN_PATH
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import java.util.*

object ArenaOrdsService {

    val env = Environment()

    suspend fun hentMeldekort(httpClient: HttpClient) {
        httpClient.get<String>("") {
            setupOrdsRequest()
        }
    }

    fun hentToken(): OrdsToken? {
        return cache.get("ordsToken", this::hentOrdsToken)
    }

    private fun hentOrdsToken(): OrdsToken {
        println("Cache timet ut. Henter token")
        var token = OrdsToken(null, null, null)

        if (isCurrentlyRunningOnNais()) {
            runBlocking {
                token = client.post("${env.ordsUrl}$ARENA_ORDS_TOKEN_PATH") {
                    setupTokenRequest()
                }
            }
        } else {
            println("Henter ikke token da appen kjører lokalt")
            token = token.copy(accessToken = "token")
        }

        return token
    }

    private fun HttpRequestBuilder.setupOrdsRequest() {
        headers.append("Accept", "application/xml")
        headers.append("Authorization","Bearer ${hentToken()}?grant_type=client_credentials")
    }

    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.ordsClientId}:${env.ordsClientSecret}"
        headers.append("Authorization", "Basic ${Base64.getEncoder().encode(base.toByteArray())}")
    }
}
