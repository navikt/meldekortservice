package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.DokumentInfo
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.JournalpostResponse
import no.nav.meldeplikt.meldekortservice.utils.JOURNALPOST_PATH
import no.nav.meldeplikt.meldekortservice.utils.STS_PATH
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class DokarkivServiceTest {

    @Test
    fun `skal sende data til dokarkiv og faa response tilbake`() {
        test(HttpStatusCode.OK)
    }

    @Test
    fun `skal sende data til dokarkiv og faa response tilbake selv om 409 Conflict`() {
        // createJournalpost skal returnere 409 når journalpost med denne eksternReferanseId allerede eksisterer
        // Men samtidig må createJournalpost returnere vanlig JournalpostReponse
        test(HttpStatusCode.Conflict)
    }

    private fun test(status: HttpStatusCode) {
        runBlocking {
            mockkStatic(::isCurrentlyRunningOnNais)
            every { isCurrentlyRunningOnNais() } returns true

            val env = Environment()

            val journalpost = this::class.java.getResource("/journalpost.json")
            val journalpostResponse = JournalpostResponse(
                journalpostId = 1234567890,
                journalstatus = "M",
                melding = "MELDING FRA DOKARKIV",
                journalpostferdigstilt = true,
                dokumenter = listOf(
                    DokumentInfo(1234567891)
                )
            )

            val base = "${env.srvMeldekortservice.username}:${env.srvMeldekortservice.password}"
            val authHederValue = "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}"
            val token = AccessToken("dG9rZW4=", "Bearer", 3600)

            val client = HttpClient(MockEngine) {
                install(ContentNegotiation) {
                    register(
                        ContentType.Application.Json,
                        JacksonConverter(defaultObjectMapper)
                    )
                }
                expectSuccess = false
                engine {
                    addHandler { request ->
                        if (
                            request.method == HttpMethod.Post
                            && request.url.protocol.name + "://" + request.url.host == env.dokarkivUrl
                            && request.url.encodedPath == JOURNALPOST_PATH
                            && request.url.parameters.contains("forsoekFerdigstill", "true")
                            && request.headers.contains(HttpHeaders.Authorization, "Bearer " + token.accessToken)
                        ) {
                            respond(
                                defaultObjectMapper.writeValueAsString(journalpostResponse),
                                status = status,
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            )
                        } else if (
                            request.method == HttpMethod.Post
                            && request.url.protocol.name + "://" + request.url.host == env.stsNaisUrl
                            && request.url.encodedPath == STS_PATH
                            && request.url.parameters.contains("grant_type", "client_credentials")
                            && request.url.parameters.contains("scope", "openid")
                            && request.headers.contains(HttpHeaders.Authorization, authHederValue)
                        ) {
                            respond(
                                defaultObjectMapper.writeValueAsString(token),
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            )
                        } else {
                            respondError(HttpStatusCode.BadRequest)
                        }
                    }
                }
            }

            val dokarkivService = DokarkivService(client, env)
            val response = dokarkivService.createJournalpost(
                defaultObjectMapper.readValue(
                    journalpost,
                    Journalpost::class.java
                )
            )

            assertEquals(journalpostResponse.journalpostId, response.journalpostId)
            assertEquals(journalpostResponse.dokumenter[0].dokumentInfoId, response.dokumenter[0].dokumentInfoId)
        }
    }
}
