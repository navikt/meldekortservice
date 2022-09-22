package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import no.nav.cache.CacheConfig
import no.nav.cache.CacheUtils
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.database.Database
import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.database.hentAlleKallLogg
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.DokumentInfo
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.JournalpostResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.StaticVars.Companion.currentCallId
import no.nav.meldeplikt.meldekortservice.utils.StaticVars.Companion.defaultDbService
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class DokarkivServiceTest {

    @Test
    fun `skal sende data til dokarkiv og faa response tilbake`() {
        val database = H2Database("dokarkivservicetest1")

        test(database, HttpStatusCode.OK)

        database.closeConnection()
    }

    @Test
    fun `skal sende data til dokarkiv og faa response tilbake selv om 409 Conflict`() {
        val database = H2Database("dokarkivservicetest2")

        // createJournalpost skal returnere 409 når journalpost med denne eksternReferanseId allerede eksisterer
        // Men samtidig må createJournalpost returnere vanlig JournalpostReponse
        test(database, HttpStatusCode.Conflict)

        database.closeConnection()
    }

    private fun test(database: Database, status: HttpStatusCode) {
        runBlocking {
            //
            // Prepare
            //
            CACHE = CacheUtils.buildCache(CacheConfig.DEFAULT.withTimeToLiveMillis(CACHE_TIMEOUT))
            val env = Environment()

            defaultDbService = DBService(database)

            currentCallId = "some_call_id"

            val journalpostFile = this::class.java.getResource("/journalpost.json")
            val journalpost = defaultObjectMapper.readValue(
                journalpostFile,
                Journalpost::class.java
            )
            val journalpostRequest = "Sent request:\n" +
                    "POST ${env.dokarkivUrl}:443$JOURNALPOST_PATH?forsoekFerdigstill=true\n" +
                    "Authorization: Bearer dG9rZW4=\n" +
                    "X-Request-ID: $currentCallId\n" +
                    "Accept: application/json\n" +
                    "Accept-Charset: UTF-8\n" +
                    "\n" +
                    defaultObjectMapper.disable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(journalpost) +
                    "\n"
            val jpRespObject = JournalpostResponse(
                journalpostId = 1234567890,
                journalstatus = "M",
                melding = "MELDING FRA DOKARKIV",
                journalpostferdigstilt = true,
                dokumenter = listOf(
                    DokumentInfo(1234567891)
                )
            )
            val journalpostResponse = "Received response:\n" +
                    "HTTP/1.1 ${status.value} ${status.description}\n" +
                    "Content-Type: application/json\n" +
                    "\n" +
                    defaultObjectMapper.disable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(jpRespObject) +
                    "\n"

            val base = "${env.srvMeldekortservice.username}:${env.srvMeldekortservice.password}"
            val authHederValue = "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}"
            val token = AccessToken("dG9rZW4=", "Bearer", 3600)
            val tokenRequest = "Sent request:\n" +
                    "POST ${env.stsNaisUrl}:443$STS_PATH?grant_type=client_credentials&scope=openid\n" +
                    "Authorization: $authHederValue\n" +
                    "X-Request-ID: $currentCallId\n" +
                    "Accept: application/json\n" +
                    "Accept-Charset: UTF-8\n" +
                    "\n" +
                    "EmptyContent\n"
            val tokenResponse = "Received response:\n" +
                    "HTTP/1.1 200 OK\n" +
                    "Content-Type: application/json\n" +
                    "\n" +
                    defaultObjectMapper.disable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(token) +
                    "\n"

            val client = HttpClient(MockEngine) {
                defaultHttpClientConfig()

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
                                defaultObjectMapper.writeValueAsString(jpRespObject),
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

            mockkStatic(::isCurrentlyRunningOnNais)
            every { isCurrentlyRunningOnNais() } returns true

            //
            // Run
            //
            val response = dokarkivService.createJournalpost(journalpost)

            //
            // Check
            //
            assertEquals(jpRespObject.journalpostId, response.journalpostId)
            assertEquals(jpRespObject.dokumenter[0].dokumentInfoId, response.dokumenter[0].dokumentInfoId)

            val kallLoggListe = database.dbQuery { hentAlleKallLogg() }
            assertEquals(2, kallLoggListe.size)

            val kall1 = kallLoggListe[0]
            assertEquals("REST", kall1.type)
            assertEquals("UT", kall1.kallRetning)
            assertEquals("POST", kall1.method)
            assertEquals(STS_PATH, kall1.operation)
            assertEquals(200, kall1.status)
            assertEquals(tokenRequest, kall1.request)
            assertEquals(tokenResponse, kall1.response)
            assertEquals("", kall1.logginfo)

            val kall2 = kallLoggListe[1]
            assertEquals("REST", kall2.type)
            assertEquals("UT", kall2.kallRetning)
            assertEquals("POST", kall2.method)
            assertEquals(JOURNALPOST_PATH, kall2.operation)
            assertEquals(status.value, kall2.status)
            assertEquals(journalpostRequest, kall2.request)
            assertEquals(journalpostResponse, kall2.response)
            assertEquals("", kall2.logginfo)
        }
    }
}
