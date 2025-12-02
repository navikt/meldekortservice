package no.nav.meldeplikt.meldekortservice.config

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.server.testing.testApplication
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
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CallLoggingPluginTest : TestBase() {

    private lateinit var database: H2Database
    private lateinit var dbService: DBService

    @Test
    fun `skal lagre request og response med FNR i token`() = testApplication {
        //
        // Prepare
        //
        val id: Long = 1

        database = H2Database("IncomingCallLoggingPluginTest1")
        dbService = DBService(database)

        defaultDbService = dbService
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "", "")

        val callId = UUID.randomUUID().toString()
        val token = issueTokenWithSub()

        val xml = """
            |<Meldekort>
            |    <Hode>
            |        <PersonId><Verdi>1</Verdi></PersonId>
            |        <Fodselsnr><Verdi>$DUMMY_FNR</Verdi></Fodselsnr>
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
                "GET localhost:80$MELDEKORT_PATH?meldekortId=${id} HTTP/1.1\n" +
                "Authorization: Bearer $token\n" +
                "X-Request-ID: $callId\n" +
                "Accept-Charset: UTF-8\n" +
                "Accept: */*\n" +
                "User-Agent: ktor-client\n" +
                "Content-Length: 0\n" +
                "\n" +
                "\n"
        val expectedInnResponseStart = "" +
                "200 OK\n" +
                "X-Request-ID: $callId\n"
        val expectedInnResponseEnd = "" +
                "Transfer-Encoding: chunked\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                "$json\n"
        val expectedUtRequest = "" +
                "GET https://dummyurl.nav.no:443/api/v2/meldeplikt/meldekort/detaljer?meldekortId=1\n" +
                "Accept: [application/xml; charset=UTF-8,application/json]\n" +
                "Authorization: Bearer $DUMMY_TOKEN\n" +
                "Accept-Charset: UTF-8\n" +
                "X-Request-ID: $callId\n" +
                "\n" +
                "EmptyContent\n"
        val expectedUtResponse = "" +
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
                mockArenaOrdsService = arenaOrdsService
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
        assertTrue(kall1.response.startsWith(expectedInnResponseStart))
        assertTrue(kall1.response.replace("NULL", "").endsWith(expectedInnResponseEnd))
        assertEquals("", kall1.logginfo)
        assertEquals(DUMMY_FNR, kall1.ident)

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

        database.closeConnection()
    }

    @Test
    fun `skal lagre request og response naar feil`() = testApplication {
        //
        // Prepare
        //
        val id: Long = 1

        database = H2Database("IncomingCallLoggingPluginTest2")
        dbService = DBService(database)

        defaultDbService = dbService
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "", "")

        val callId = UUID.randomUUID().toString()
        val token = issueTokenWithSub()

        val xml = """
            |<Meldekort>
            |    <Hode>
            |        <PersonId><Verdi>1</Verdi></PersonId>
            |        <Fodselsnr><Verdi>01020312346</Verdi></Fodselsnr>
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

        val json = "{\"error\":\"Personidentifikator matcher ikke. Bruker kan derfor ikke hente ut meldekortdetaljer.\"}"

        val expectedInnRequest = "" +
                "GET localhost:80$MELDEKORT_PATH?meldekortId=$id HTTP/1.1\n" +
                "Authorization: Bearer $token\n" +
                "X-Request-ID: $callId\n" +
                "Accept-Charset: UTF-8\n" +
                "Accept: */*\n" +
                "User-Agent: ktor-client\n" +
                "Content-Length: 0\n" +
                "\n" +
                "\n"
        val expectedInnResponseStart = "" +
                "400 Bad Request\n" +
                "X-Request-ID: $callId\n"
        val expectedInnResponseEnd = "" +
                "Transfer-Encoding: chunked\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                "$json\n"
        val expectedUtRequest = "" +
                "GET https://dummyurl.nav.no:443/api/v2/meldeplikt/meldekort/detaljer?meldekortId=$id\n" +
                "Accept: [application/xml; charset=UTF-8,application/json]\n" +
                "Authorization: Bearer $DUMMY_TOKEN\n" +
                "Accept-Charset: UTF-8\n" +
                "X-Request-ID: $callId\n" +
                "\n" +
                "EmptyContent\n"
        val expectedUtResponse = "" +
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
                mockArenaOrdsService = arenaOrdsService
            )
        }

        //
        // Run
        //
        val response1 = client.get("$MELDEKORT_PATH?meldekortId=$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.XRequestId, callId)
        }

        val response2 = client.get("$API_PATH/v2/historiskemeldekort?antallMeldeperioder=1") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("ident", DUMMY_FNR)
            header(HttpHeaders.XRequestId, callId)
        }

        //
        // Check
        //
        assertEquals(HttpStatusCode.BadRequest, response1.status)
        assertEquals(json, response1.bodyAsText())
        assertEquals(HttpStatusCode.ServiceUnavailable, response2.status)

        val kallLoggListe = database.dbQuery { hentAlleKallLogg() }
        assertEquals(4, kallLoggListe.size)

        val kall1 = kallLoggListe[0]
        assertEquals(callId, kall1.korrelasjonId)
        assertEquals("REST", kall1.type)
        assertEquals("INN", kall1.kallRetning)
        assertEquals("GET", kall1.method)
        assertEquals(MELDEKORT_PATH, kall1.operation)
        assertEquals(400, kall1.status)
        assertEquals(expectedInnRequest, kall1.request)
        assertTrue(kall1.response.startsWith(expectedInnResponseStart))
        assertTrue(kall1.response.replace("NULL", "").endsWith(expectedInnResponseEnd))
        assertEquals("", kall1.logginfo)
        assertEquals(DUMMY_FNR, kall1.ident)

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
        assertEquals("", kall2.ident)

        val kall4 = kallLoggListe[3]
        assertEquals(callId, kall4.korrelasjonId)
        assertEquals("REST", kall4.type)
        assertEquals("UT", kall4.kallRetning)
        assertEquals("GET", kall4.method)
        assertEquals(ARENA_ORDS_HENT_HISTORISKE_MELDEKORT.replace("?", ""), kall4.operation)
        assertEquals(200, kall4.status)
        assertEquals("", kall4.logginfo)
        assertEquals(DUMMY_FNR, kall4.ident)

        database.closeConnection()
    }


    @Test
    fun `skal lagre request og response med system id i token`() = testApplication {
        //
        // Prepare
        //
        database = H2Database("IncomingCallLoggingPluginTest3")
        dbService = DBService(database)

        defaultDbService = dbService
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "", "")

        val callId = UUID.randomUUID().toString()
        val token = issueTokenWithSub("HKZpfaHyWadeOouYlitjrI-KffTm222X5rrV3xDqfKQ")

        val xml =
            """<Person><personId>1</personId><Etternavn>test</Etternavn><Fornavn>test</Fornavn><Maalformkode>test</Maalformkode><Meldeform>test</Meldeform><meldekortListe/><antallGjenstaaendeFeriedager>10</antallGjenstaaendeFeriedager><fravaerListe/></Person>"""
        val json = "{\"personId\":1,\"etternavn\":\"test\",\"fornavn\":\"test\",\"maalformkode\":\"test\",\"meldeform\":\"test\",\"meldekortListe\":[],\"antallGjenstaaendeFeriedager\":10,\"fravaerListe\":[]}"
        val expectedInnRequest = "GET localhost:80/meldekortservice/api/v2/historiskemeldekort?antallMeldeperioder=1 HTTP/1.1\n" +
                "Authorization: Bearer $token\n" +
                "ident: $DUMMY_FNR\n" +
                "X-Request-ID: $callId\n" +
                "Accept-Charset: UTF-8\n" +
                "Accept: */*\n" +
                "User-Agent: ktor-client\n" +
                "Content-Length: 0\n" +
                "\n" +
                "\n"
        val expectedInnResponseStart = "200 OK\n" +
                "X-Request-ID: $callId\n"
        val expectedInnResponseEnd =
                "Transfer-Encoding: chunked\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                json + "\n"
        val expectedUtRequest = "GET https://dummyurl.nav.no:443/api/v2/meldeplikt/meldekort/historiske?antMeldeperioder=1\n" +
                "Accept: [application/xml; charset=UTF-8,application/json]\n" +
                "Authorization: Bearer $DUMMY_TOKEN\n" +
                "fnr: $DUMMY_FNR\n" +
                "Accept-Charset: UTF-8\n" +
                "X-Request-ID: $callId\n" +
                "\n" +
                "EmptyContent\n"
        val expectedUtResponse = "HTTP/1.1 200 OK\n" +
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
                mockArenaOrdsService = arenaOrdsService
            )
        }

        //
        // Run
        //
        val response = client.get("$API_PATH/v2/historiskemeldekort?antallMeldeperioder=1") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("ident", DUMMY_FNR)
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
        assertEquals("$API_PATH/v2/historiskemeldekort", kall1.operation)
        assertEquals(200, kall1.status)
        assertEquals(expectedInnRequest, kall1.request)
        assertTrue(kall1.response.startsWith(expectedInnResponseStart))
        assertTrue(kall1.response.replace("NULL", "").endsWith(expectedInnResponseEnd))
        assertEquals("", kall1.logginfo)
        assertEquals(DUMMY_FNR, kall1.ident)

        val kall2 = kallLoggListe[1]
        assertEquals(callId, kall2.korrelasjonId)
        assertEquals("REST", kall2.type)
        assertEquals("UT", kall2.kallRetning)
        assertEquals("GET", kall2.method)
        assertEquals(ARENA_ORDS_HENT_HISTORISKE_MELDEKORT.replace("?", ""), kall2.operation)
        assertEquals(200, kall2.status)
        assertEquals(expectedUtRequest, kall2.request)
        assertEquals(expectedUtResponse, kall2.response)
        assertEquals("", kall2.logginfo)

        database.closeConnection()
    }
}
