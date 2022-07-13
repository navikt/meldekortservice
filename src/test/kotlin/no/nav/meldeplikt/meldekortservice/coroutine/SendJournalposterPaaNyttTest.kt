package no.nav.meldeplikt.meldekortservice.coroutine

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.ktor.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.database.hentAlleMidlertidigLagredeJournalposter
import no.nav.meldeplikt.meldekortservice.database.hentJournalpostData
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.DokumentInfo
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.JournalpostResponse
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.utils.JOURNALPOST_PATH
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SendJournalposterPaaNyttTest {
    private lateinit var database: H2Database
    private lateinit var dbService: DBService
    private val journalpostJson = this::class.java.getResource("/journalpost.json")!!.readText()
    private val journalpost = jacksonObjectMapper().readValue(
        journalpostJson,
        Journalpost::class.java
    )

    @BeforeEach
    fun setUp() {
        database = H2Database("journalposttest")
        dbService = DBService(database)
    }

    @AfterEach
    fun tearDown() {
        database.closeConnection()
    }

    @Test
    fun `skal sende journalpost, lagre journalpost data og slette midlertidig journalpost naar OK`() {
        val journalpostId = 123456780L
        val dokumentInfoId = 123456781L
        val journalpostResponse = JournalpostResponse(
            journalpostId = journalpostId,
            journalstatus = "M",
            melding = "MELDING FRA DOKARKIV",
            journalpostferdigstilt = true,
            dokumenter = listOf(
                DokumentInfo(dokumentInfoId)
            )
        )

        val env = Environment()

        val httpClient = HttpClient(MockEngine) {
            install(JsonFeature) {
                serializer = JacksonSerializer { defaultObjectMapper }
            }
            expectSuccess = false
            engine {
                addHandler { request ->
                    respondError(HttpStatusCode.ServiceUnavailable)
                    if (
                        request.method == HttpMethod.Post &&
                        request.url.protocol.name + "://" + request.url.host == env.dokarkivUrl &&
                        request.url.fullPath == "$JOURNALPOST_PATH?forsoekFerdigstill=true"
                    ) {
                        respond(
                            ObjectMapper().writeValueAsString(journalpostResponse),
                            HttpStatusCode.Conflict,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        )
                    } else {
                        respondError(HttpStatusCode.BadRequest)
                    }
                }
            }
        }
        val dokarkivService = DokarkivService(httpClient)

        // Sjekk at det ikke finnes midlertidig lagrede journalposter
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(0, journalpostData.size)
        }

        // Lagre journalpost
        dbService.lagreJournalpostMidlertidig(journalpost)

        // Sjekk at det finnes 1 midlertidig lagret journalpost med 0 i retries
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
            assertEquals(0, journalpostData[0].second) // Retries
        }

        // Sjekk at det ikke finnes journalpost data
        runBlocking {
            val result = database.dbQuery { hentJournalpostData() }
            assertEquals(0, result.size)
        }

        // Prøv å sende på nytt
        val sendJournalposterPaaNytt = SendJournalposterPaaNytt(dbService, dokarkivService, 10_000L, 0)
        sendJournalposterPaaNytt.start()
        Thread.sleep(1_000)
        sendJournalposterPaaNytt.stop()

        // Sjekk at det ikke finnes midlertidig lagrede journalposter
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(0, journalpostData.size)
        }

        // Sjekk at det finnes journalpost data
        runBlocking {
            val result = database.dbQuery { hentJournalpostData() }
            assertEquals(1, result.size)
            assertEquals(journalpostId, result[0].first)
            assertEquals(dokumentInfoId, result[0].second)
            assertEquals(1011121315, result[0].third) // MeldekortId kommer fra journalpost.json
        }
    }

    @Test
    fun `skal sende journalpost og ikke slette midlertidig journalpost naar ikke OK`() {
        val dokarkivService = mockk<DokarkivService>()
        coEvery { dokarkivService.createJournalpost(any()) } throws Exception()

        // Sjekk at det ikke finnes midlertidig lagrede journalposter
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(0, journalpostData.size)
        }

        // Lagre journalpost
        dbService.lagreJournalpostMidlertidig(journalpost)

        // Sjekk at det finnes 1 midlertidig lagret journalpost med 0 i retries
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
            assertEquals(0, journalpostData[0].second) // Retries
        }

        // Prøv å sende på nytt (får feil)
        val sendJournalposterPaaNytt = SendJournalposterPaaNytt(dbService, dokarkivService, 10_000L, 0)
        sendJournalposterPaaNytt.start()
        Thread.sleep(1_000)
        sendJournalposterPaaNytt.stop()

        // Sjekk at det finnes 1 midlertidig lagret journalpost med 1 i retries
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
            assertEquals(1, journalpostData[0].second) // Retries
        }
    }
}