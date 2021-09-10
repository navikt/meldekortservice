package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
    },
    private val env: Environment = Environment()
) {
    private val log = getLogger(DokarkivService::class)

    suspend fun createJournalpost(journalpost: Journalpost): JournalpostResponse {
        val response = httpClient.post<String>("${env.dokarkivUrl}$JOARK_JOURNALPOST_PATH") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer " + hentToken().accessToken)
            body = journalpost
        }

        return jacksonObjectMapper().readValue(response, JournalpostResponse::class.java)
    }

    private fun hentToken(): AccessToken {
        return CACHE.get("stsToken", this::hentAccessToken)
    }

    private fun hentAccessToken(): AccessToken {
        log.info("Cache timet ut. Henter token")
        var token = AccessToken(null, null, null)

        if (isCurrentlyRunningOnNais()) {
            runBlocking {
                token = httpClient.post("${env.securityTokenService}$STS_PATH?grant_type=client_credentials&scope=openid") {
                    setupTokenRequest()
                }
            }
        } else {
            log.info("Henter ikke token da appen kjører lokalt")
            token = token.copy(accessToken = "token")
        }

        return token
    }

    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.srvMeldekortservice.username}:${env.srvMeldekortservice.password}"
        header("Authorization", "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}")
    }
}