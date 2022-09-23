package no.nav.meldeplikt.meldekortservice.config

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.locations.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import no.nav.meldeplikt.meldekortservice.api.TestBase
import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.database.hentAlleKallLogg
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortdetaljerMapper
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Meldekort
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.*
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalLocationsAPI
class CallLoggingPluginTest : TestBase() {

    private lateinit var database: H2Database
    private lateinit var dbService: DBService

    @BeforeEach
    fun setUp() {
        database = H2Database("IncomingCallLoggingPluginTest")
        dbService = DBService(database)
    }

    @AfterEach
    fun tearDown() {
        database.closeConnection()
    }

    @Test
    fun `skal lagre request og response`() = testApplication {
        //
        // Prepare
        //
        val id: Long = 1

        defaultDbService = dbService
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

        val callId = UUID.randomUUID().toString()
        val token = issueTokenWithSub()

        val xml = """
            |<Meldekort>
            |    <Hode>
            |        <PersonId><Verdi>1</Verdi></PersonId>
            |        <Fodselsnr><Verdi>01020312345</Verdi></Fodselsnr>
            |        <MeldekortId><Verdi>1</Verdi></MeldekortId>
            |        <Meldeperiode>2</Meldeperiode>
            |        <Arkivnokkel>test</Arkivnokkel>
            |        <KortType>03</KortType>
            |        <Kommentar>test</Kommentar>
            |    </Hode>
            |    <Spm>
            |        <Arbeidssoker><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></Arbeidssoker>
            |        <Arbeidet><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></Arbeidet>
            |        <Syk><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></Syk>
            |        <AnnetFravaer><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></AnnetFravaer>
            |        <Kurs><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></Kurs>
            |        <Forskudd><Verdi>false</Verdi></Forskudd>
            |        <MeldekortDager/>
            |        <Signatur><Verdi>false</Verdi></Signatur>
            |    </Spm>
            |</Meldekort>""".trimMargin()

        val meldekort = mapFraXml(xml, Meldekort::class.java)
        val meldekortdetaljer = MeldekortdetaljerMapper.mapOrdsMeldekortTilMeldekortdetaljer(meldekort)
        val json = defaultObjectMapper.disable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(meldekortdetaljer)
        val expectedInnRequest = "" +
                "Received request:\n" +
                "GET localhost:80$MELDEKORT_PATH?meldekortId=${id} HTTP/1.1\n" +
                "Authorization: Bearer $token\n" +
                "X-Request-ID: $callId\n" +
                "Accept-Charset: UTF-8\n" +
                "Accept: */*\n" +
                "Content-Length: 0\n" +
                "\n" +
                "\n"
        val expectedInnResponseStart = "" +
                "Sent response:\n" +
                "200 OK\n" +
                "X-Request-ID: $callId\n"
        val expectedInnResponseEnd = "" +
                "Server: Ktor/2.0.3\n" +
                "\n" +
                "$json\n"
        val expectedUtRequest = "" +
                "Sent request:\n" +
                "GET https://dummyurl.nav.no:443/api/v1/meldeplikt/meldekort/detaljer?meldekortId=1\n" +
                "Accept: [application/xml; charset=UTF-8,application/json]\n" +
                "Authorization: Bearer $DUMMY_TOKEN\n" +
                "X-Request-ID: $callId\n" +
                "Accept-Charset: UTF-8\n" +
                "\n" +
                "EmptyContent\n"
        val expectedUtResponse = "" +
                "Received response:\n" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type: application/xml\n" +
                "\n" +
                xml + "\n"

        val arenaOrdsClient = HttpClient(MockEngine) {
            defaultHttpClientConfig()

            engine {
                addHandler {
                    respond(
                        xml,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Xml.toString())
                    )
                }
            }
        }
        val arenaOrdsService = ArenaOrdsService(arenaOrdsClient)

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                env = env,
                mockDBService = dbService,
                mockFlywayConfig = flywayConfig,
                mockArenaOrdsService = arenaOrdsService,
                mockKontrollService = kontrollService,
                mockDokarkivService = dokarkivService
            )
        }

        //
        // Run
        //
        val response = client.get("$MELDEKORT_PATH?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.XRequestId, callId)
        }

        //
        // Check
        //
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(json, response.bodyAsText().replace("NULL", ""))

        val kallLoggListe = database.dbQuery { hentAlleKallLogg() }
        assertEquals(2, kallLoggListe.size)

        val kall1 = kallLoggListe[0]
        assertEquals(callId, kall1.korrelasjonId)
        assertEquals("REST", kall1.type)
        assertEquals("INN", kall1.kallRetning)
        assertEquals("GET", kall1.method)
        assertEquals(MELDEKORT_PATH, kall1.operation)
        assertEquals(200, kall1.status)
        assertEquals(expectedInnRequest, kall1.request)
        assertTrue(kall1.response?.startsWith(expectedInnResponseStart) == true)
        assertTrue(kall1.response?.replace("NULL", "")?.endsWith(expectedInnResponseEnd) == true)
        assertEquals("", kall1.logginfo)

        val kall2 = kallLoggListe[1]
        assertEquals(callId, kall2.korrelasjonId)
        assertEquals("REST", kall2.type)
        assertEquals("UT", kall2.kallRetning)
        assertEquals("GET", kall2.method)
        assertEquals(ARENA_ORDS_HENT_MELDEKORTDETALJER.replace("?meldekortId=", ""), kall2.operation)
        assertEquals(200, kall2.status)
        assertEquals(expectedUtRequest, kall2.request)
        assertEquals(expectedUtResponse, kall2.response)
        assertEquals("", kall2.logginfo)
    }
}
