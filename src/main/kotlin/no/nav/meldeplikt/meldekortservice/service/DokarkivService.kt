package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.CACHE
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.JournalpostResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import java.util.*

class DokarkivService(
    private val httpClient: HttpClient = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer { objectMapper }
        }
        expectSuccess = false
    },
    private val env: Environment = Environment()
) {

    suspend fun createJournalpost(journalpost: Journalpost): JournalpostResponse {
        return httpClient.post("${env.dokarkivUrl}$JOURNALPOST_PATH?forsoekFerdigstill=true") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer " + hentToken().accessToken)
            body = journalpost
        }
    }

    private fun hentToken(): AccessToken {
        return CACHE.get("stsToken", this::hentAccessToken)
    }

    private fun hentAccessToken(): AccessToken {
        defaultLog.info("Cache timet ut. Henter token")
        var token = AccessToken(null, null, null)

        if (isCurrentlyRunningOnNais()) {
            runBlocking {
                token = httpClient.post("${env.stsNaisUrl}$STS_PATH?grant_type=client_credentials&scope=openid") {
                    setupTokenRequest()
                }
            }
        } else {
            defaultLog.info("Henter ikke token da appen kjører lokalt")
            token = token.copy(accessToken = "token")
        }

        return token
    }

    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.srvMeldekortservice.username}:${env.srvMeldekortservice.password}"
        header("Authorization", "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}")
    }
}