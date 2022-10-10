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
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.*
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Meldekort
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.utils.*
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalLocationsAPI
class CallLoggingPluginTest : TestBase() {

    private lateinit var database: H2Database
    private lateinit var dbService: DBService

    @Test
    fun `skal lagre request og response`() = testApplication {
        //
        // Prepare
        //
        val id: Long = 1

        database = H2Database("IncomingCallLoggingPluginTest1")
        dbService = DBService(database)

        defaultDbService = dbService
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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
                "Transfer-Encoding: chunked\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
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
        assertTrue(kall1.response.startsWith(expectedInnResponseStart))
        assertTrue(kall1.response.replace("NULL", "").endsWith(expectedInnResponseEnd))
        assertEquals(DUMMY_FNR, kall1.logginfo)

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
    fun `skal ikke lagre request og response content for opprettJournalpost`() = testApplication {
        //
        // Prepare
        //
        database = H2Database("IncomingCallLoggingPluginTest2")
        dbService = DBService(database)
        defaultDbService = dbService
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

        val meldekortId = "123456779"
        val journalpostId = 123456780L
        val dokumentInfoId = 123456781L

        val journalpost = Journalpost(
            journalposttype = Journalposttype.INNGAAENDE,
            avsenderMottaker = AvsenderMottaker(
                id = DUMMY_FNR,
                idType = AvsenderIdType.FNR,
                navn = "Test Testesen"
            ),
            bruker = Bruker(
                id = DUMMY_FNR,
                idType = BrukerIdType.FNR
            ),
            tema = Tema.AAP,
            tittel = "Test",
            kanal = "NAV_NO",
            journalfoerendeEnhet = "9999",
            eksternReferanseId = "1",
            datoMottatt = "2022-10-07",
            tilleggsopplysninger = listOf(Tilleggsopplysning("meldekortId", meldekortId)),
            sak = Sak(
                sakstype = Sakstype.GENERELL_SAK
            ),
            dokumenter = null
        )

        val journalpostResponse = JournalpostResponse(
            journalpostId = journalpostId,
            journalstatus = "M",
            melding = "MELDING FRA DOKARKIV",
            journalpostferdigstilt = true,
            dokumenter = listOf(
                DokumentInfo(dokumentInfoId)
            )
        )

        val callId = UUID.randomUUID().toString()
        val innToken = issueTokenWithSub()
        val base = "${env.srvMeldekortservice.username}:${env.srvMeldekortservice.password}"
        val authHeaderValue = "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}"
        val aceessTokenContent = "dG9rZW4="
        val accessToken = AccessToken(aceessTokenContent, "Bearer", 3600)

        val httpClient = HttpClient(MockEngine) {
            defaultHttpClientConfig()

            engine {
                addHandler { request ->
                    if (
                        request.method == HttpMethod.Post
                        && request.url.protocol.name + "://" + request.url.host == env.dokarkivUrl
                        && request.url.encodedPath == JOURNALPOST_PATH
                        && request.url.parameters.contains("forsoekFerdigstill", "true")
                        && request.headers.contains(HttpHeaders.Authorization, "Bearer " + accessToken.accessToken)
                    ) {
                        respond(
                            defaultObjectMapper.writeValueAsString(journalpostResponse),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        )
                    } else if (
                        request.method == HttpMethod.Post
                        && request.url.protocol.name + "://" + request.url.host == env.stsNaisUrl
                        && request.url.encodedPath == STS_PATH
                        && request.url.parameters.contains("grant_type", "client_credentials")
                        && request.url.parameters.contains("scope", "openid")
                        && request.headers.contains(HttpHeaders.Authorization, authHeaderValue)
                    ) {
                        respond(
                            defaultObjectMapper.writeValueAsString(accessToken),
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        )
                    } else {
                        respondError(HttpStatusCode.BadRequest)
                    }
                }


                addHandler {
                    respond(
                        defaultObjectMapper.writeValueAsString(journalpostResponse),
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
        }
        val dokarkivService = DokarkivService(httpClient)

        val expectedInnRequest = "" +
                "Received request:\n" +
                "POST localhost:80$OPPRETT_JOURNALPOST_PATH HTTP/1.1\n" +
                "Authorization: Bearer $innToken\n" +
                "X-Request-ID: $callId\n" +
                "Accept-Charset: UTF-8\n" +
                "Accept: */*\n" +
                "Content-Length: 537\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "JOURNALPOST\n"
        val expectedInnResponseStart = "" +
                "Sent response:\n" +
                "200 OK\n" +
                "X-Request-ID: $callId\n"
        val expectedInnResponseEnd = "" +
                "Server: Ktor/2.0.3\n" +
                "Content-Length: 21\n" +
                "Content-Type: text/plain; charset=UTF-8\n" +
                "\n" +
                "Journalpost opprettet\n"
        val expectedStsUtRequest = "" +
                "Sent request:\n" +
                "POST ${env.stsNaisUrl}:443$STS_PATH?grant_type=client_credentials&scope=openid\n" +
                "Authorization: $authHeaderValue\n" +
                "X-Request-ID: $callId\n" +
                "Accept: application/json\n" +
                "Accept-Charset: UTF-8\n" +
                "\n" +
                "EmptyContent\n"
        val expectedStsUtResponse = "" +
                "Received response:\n" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\n" +
                "  \"access_token\" : \"$aceessTokenContent\",\n" +
                "  \"token_type\" : \"Bearer\",\n" +
                "  \"expires_in\" : 3600\n" +
                "}\n"
        val expectedUtRequest = "" +
                "Sent request:\n" +
                "POST ${env.dokarkivUrl}:443$JOURNALPOST_PATH?forsoekFerdigstill=true\n" +
                "Authorization: Bearer $aceessTokenContent\n" +
                "X-Request-ID: $callId\n" +
                "Accept: application/json\n" +
                "Accept-Charset: UTF-8\n" +
                "\n" +
                "JOURNALPOST\n"
        val expectedUtResponse = "" +
                "Received response:\n" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\n" +
                "  \"journalpostId\" : $journalpostId,\n" +
                "  \"journalstatus\" : \"M\",\n" +
                "  \"melding\" : \"MELDING FRA DOKARKIV\",\n" +
                "  \"journalpostferdigstilt\" : true,\n" +
                "  \"dokumenter\" : [ {\n" +
                "    \"dokumentInfoId\" : $dokumentInfoId\n" +
                "  } ]\n" +
                "}\n"

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
        val response = client.post(OPPRETT_JOURNALPOST_PATH) {
            header(HttpHeaders.Authorization, "Bearer $innToken")
            header(HttpHeaders.XRequestId, callId)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(defaultObjectMapper.writeValueAsString(journalpost))
        }

        //
        // Check
        //
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Journalpost opprettet", response.bodyAsText())

        val kallLoggListe = database.dbQuery { hentAlleKallLogg() }
        assertEquals(3, kallLoggListe.size)

        val kall1 = kallLoggListe[0]
        assertEquals(callId, kall1.korrelasjonId)
        assertEquals("REST", kall1.type)
        assertEquals("INN", kall1.kallRetning)
        assertEquals("POST", kall1.method)
        assertEquals(OPPRETT_JOURNALPOST_PATH, kall1.operation)
        assertEquals(200, kall1.status)
        assertEquals(expectedInnRequest, kall1.request)
        assertTrue(kall1.response.startsWith(expectedInnResponseStart))
        assertTrue(kall1.response.endsWith(expectedInnResponseEnd))
        assertEquals(DUMMY_FNR, kall1.logginfo)

        val kall2 = kallLoggListe[1]
        assertEquals(callId, kall2.korrelasjonId)
        assertEquals("REST", kall2.type)
        assertEquals("UT", kall2.kallRetning)
        assertEquals("POST", kall2.method)
        assertEquals(STS_PATH, kall2.operation)
        assertEquals(200, kall2.status)
        assertEquals(expectedStsUtRequest, kall2.request)
        assertEquals(expectedStsUtResponse, kall2.response.replace("\r", ""))
        assertEquals("", kall2.logginfo)

        val kall3 = kallLoggListe[2]
        assertEquals(callId, kall3.korrelasjonId)
        assertEquals("REST", kall3.type)
        assertEquals("UT", kall3.kallRetning)
        assertEquals("POST", kall3.method)
        assertEquals(JOURNALPOST_PATH, kall3.operation)
        assertEquals(200, kall3.status)
        assertEquals(expectedUtRequest, kall3.request)
        assertEquals(expectedUtResponse, kall3.response.replace("\r", ""))
        assertEquals("", kall3.logginfo)

        database.closeConnection()
    }
}
