package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.DUMMY_TOKEN
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.model.ArenaOrdsSkrivemodus
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.model.feil.OrdsException
import no.nav.meldeplikt.meldekortservice.model.meldestatus.Endring
import no.nav.meldeplikt.meldekortservice.model.meldestatus.Meldeplikt
import no.nav.meldeplikt.meldekortservice.model.meldestatus.MeldestatusResponse
import no.nav.meldeplikt.meldekortservice.model.meldestatus.Periode
import no.nav.meldeplikt.meldekortservice.utils.ARENA_ORDS_TOKEN_PATH
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ArenaOrdsServiceTest {
    private val fnr = "01020312345"
    private val id = 1234L
    private val fornavn = "Test"
    private val etternavn = "Testesen"
    private val maalformkode = "NO"
    private val meldeform = "EMELD"
    private val antallGjenstaaendeFeriedager = 10
    private val personXml =
        """
            <Person>
                <PersonId>$id</PersonId>
                <Fornavn>$fornavn</Fornavn>
                <Etternavn>$etternavn</Etternavn>
                <Maalformkode>$maalformkode</Maalformkode>
                <Meldeform>$meldeform</Meldeform>
                <MeldekortListe/>
                <AntallGjenstaaendeFeriedager>$antallGjenstaaendeFeriedager</AntallGjenstaaendeFeriedager>
                <FravaerListe/>
            </Person>""".trimIndent()


    @BeforeAll
    fun setup() {
        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
    }

    @Nested
    inner class HentMeldekort {
        @Test
        fun `hentMeldekort returnerer OK status`() {
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        if (request.url.toString() == "https://dummyurl.nav.no/api/v2/meldeplikt/meldekort") {
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                            assertEquals(fnr, request.headers["fnr"])

                            respond(
                                personXml
                            )
                        } else {
                            respondError(HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentMeldekort(fnr)

                assertEquals(id, actualResponse.personId)
                assertEquals(fornavn, actualResponse.fornavn)
                assertEquals(etternavn, actualResponse.etternavn)
                assertEquals(maalformkode, actualResponse.maalformkode)
                assertEquals(meldeform, actualResponse.meldeform)
                assertEquals(0, actualResponse.meldekortListe?.size)
                assertEquals(antallGjenstaaendeFeriedager, actualResponse.antallGjenstaaendeFeriedager)
                assertEquals(0, actualResponse.fravaerListe?.size)
            }
        }

        @Test
        fun `hentMeldekort kaster Exception`() {
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        if (request.url.encodedPath.contains("/api/v2/meldeplikt/meldekort/12345678")
                            && request.url.host.contains("dummyurl.nav.no")
                        ) {
                            respond(
                                "",
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            )
                        } else {
                            respondError(HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)
            val exception = assertThrows<OrdsException> {
                runBlocking {
                    arenaOrdsService.hentMeldekort(fnr)
                }
            }
            assertEquals("Kunne ikke hente meldekort fra Arena Ords. Status: 400", exception.localizedMessage)
        }
    }

    @Nested
    inner class HentHistoriskeMeldekort {
        @Test
        fun `hentHistoriskeMeldekort returnerer OK status`() {
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        assertEquals(HttpMethod.Get, request.method)
                        assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                        assertEquals(fnr, request.headers["fnr"])
                        assertEquals(
                            "https://dummyurl.nav.no/api/v2/meldeplikt/meldekort/historiske?antMeldeperioder=10",
                            request.url.toString()
                        )

                        respond(
                            personXml
                        )
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentHistoriskeMeldekort(fnr, 10)

                assertEquals(id, actualResponse.personId)
                assertEquals(fornavn, actualResponse.fornavn)
                assertEquals(etternavn, actualResponse.etternavn)
                assertEquals(maalformkode, actualResponse.maalformkode)
                assertEquals(meldeform, actualResponse.meldeform)
                assertEquals(0, actualResponse.meldekortListe?.size)
                assertEquals(antallGjenstaaendeFeriedager, actualResponse.antallGjenstaaendeFeriedager)
                assertEquals(0, actualResponse.fravaerListe?.size)
            }
        }
    }

    @Nested
    inner class HentMeldekortdetaljer {
        @Test
        fun `hentMeldekortdetaljer returnerer OK status`() {
            val xmlString = """<Meldekort>
|                               <Hode>
|                                   <PersonId><Verdi>1</Verdi></PersonId>
|                                   <Fodselsnr><Verdi>1234</Verdi></Fodselsnr>
|                                   <MeldekortId><Verdi>1</Verdi></MeldekortId>
|                                   <Meldeperiode>2</Meldeperiode>
|                                   <Arkivnokkel>test</Arkivnokkel>
|                                   <KortType>03</KortType>
|                                   <Kommentar>test</Kommentar>
|                               </Hode>
|                               <Spm>
|                                   <Arbeidssoker><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></Arbeidssoker>
|                                   <Arbeidet><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></Arbeidet>
|                                   <Syk><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></Syk>
|                                   <AnnetFravaer><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></AnnetFravaer>
|                                   <Kurs><SvarJa><Verdi>false</Verdi></SvarJa><SvarNei><Verdi>false</Verdi></SvarNei></Kurs>
|                                   <Forskudd><Verdi>false</Verdi></Forskudd>
|                                   <MeldekortDager/>
|                                   <Signatur><Verdi>false</Verdi></Signatur>
|                               </Spm>
|                          </Meldekort>""".trimMargin()

            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        assertEquals(HttpMethod.Get, request.method)
                        assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                        assertEquals(
                            "https://dummyurl.nav.no/api/v2/meldeplikt/meldekort/detaljer?meldekortId=1",
                            request.url.toString()
                        )

                        respond(
                            xmlString
                        )
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentMeldekortdetaljer(1)
                assertEquals(1, actualResponse.meldekortId)
            }
        }
    }

    @Nested
    inner class KopierMeldekort {
        @Test
        fun `kopierMeldekort returnerer OK status`() {
            val xmlString = """<KopierMeldekortResponse><meldekortId>123</meldekortId></KopierMeldekortResponse>"""
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        assertEquals(HttpMethod.Post, request.method)
                        assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                        assertEquals("123", request.headers["meldekortId"])
                        assertEquals(
                            "https://dummyurl.nav.no/api/v2/meldeplikt/meldekort/kopi",
                            request.url.toString()
                        )

                        respondOk(
                            xmlString
                        )
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.kopierMeldekort(123)

                assertEquals(123, actualResponse)
            }
        }

        @Test
        fun `kopierMeldekort returnerer 0 hvis ikke OK`() {
            val xmlString = """NOT XML STRING"""
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        assertEquals(HttpMethod.Post, request.method)
                        assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                        assertEquals("123", request.headers["meldekortId"])
                        assertEquals(
                            "https://dummyurl.nav.no/api/v2/meldeplikt/meldekort/kopi",
                            request.url.toString()
                        )

                        respondOk(
                            xmlString
                        )
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.kopierMeldekort(123)

                assertEquals(0, actualResponse)
            }
        }
    }

    @Nested
    inner class HentMeldestatus {
        @Test
        fun `hentMeldestatus returnerer data`() {
            val meldestatusResponse = MeldestatusResponse(
                123,
                fnr,
                "DAGP",
                true,
                listOf(
                    Meldeplikt(
                        true,
                        Periode(
                            LocalDateTime.now().minusDays(10),
                        ),
                        "",
                        Endring(
                            "R123456",
                            LocalDateTime.now().minusDays(7),
                            "E654321",
                            LocalDateTime.now()
                        ),
                    )
                )
            )

            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        if (request.url.toString() == "https://dummyurl.nav.no/api/v3/meldeplikt/meldestatus") {
                            assertEquals(HttpMethod.Post, request.method)
                            assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])

                            respond(
                                defaultObjectMapper.writeValueAsString(meldestatusResponse),
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            )
                        } else {
                            respond("", HttpStatusCode.BadRequest)
                        }
                    }
                }
            }

            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentMeldestatus(null, fnr, null)
                assertEquals(meldestatusResponse, actualResponse)
            }
        }

        @Test
        fun `hentMeldestatus kaster OrdsException når ikke kan hente meldegrupper fra Arena ORDS`() {
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler {
                        respond("", HttpStatusCode.BadRequest)
                    }
                }
            }

            val arenaOrdsService = ArenaOrdsService(client)

            val exception = assertThrows<OrdsException> {
                runBlocking {
                    arenaOrdsService.hentMeldestatus(null, fnr, null)
                }
            }
            assertEquals("Kunne ikke hente meldestatus fra Arena Ords", exception.localizedMessage)
        }

        @Test
        fun `hentMeldestatus kaster NoContentException når Arena ORDS returnerer NoContent`() {
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler {
                        respond("", HttpStatusCode.NoContent)
                    }
                }
            }

            val arenaOrdsService = ArenaOrdsService(client)

            assertThrows<NoContentException> {
                runBlocking {
                    arenaOrdsService.hentMeldestatus(null, fnr, null)
                }
            }
        }
    }

    @Nested
    inner class HentSkrivemodus {
        @Test
        fun `hentSkrivemodus returnerer true naar ORDS er i skrivemodus `() {
            val response = ArenaOrdsSkrivemodus(true)
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        if (request.url.toString() == "https://dummyurl.nav.no/api/v1/app/skrivemodus") {
                            respond(
                                defaultObjectMapper.writeValueAsString(response),
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            )
                        } else {
                            respondError(HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentSkrivemodus()

                assertEquals(true, actualResponse.skrivemodus)
            }
        }

        @Test
        fun `hentSkrivemodus returnerer false naar ORDS er utilgjengelig`() {
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        if (request.url.toString() == "https://dummyurl.nav.no/api/v1/app/dummy") {
                            respond(
                                "",
                            )
                        } else {
                            respondError(HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentSkrivemodus()

                assertEquals(false, actualResponse.skrivemodus)
            }
        }
    }

    @Test
    fun `test request retry`() {
        var count = 0
        val url = "https://not-so-dummyurl.nav.no"
        val env = Environment(url)

        val token = AccessToken(
            accessToken = DUMMY_TOKEN,
            tokenType = "bearer",
            expiresIn = 1
        )

        // Return token
        // Return Unauthorized for the first skrivemodus-request
        // Return valid response for the second skrivemodus-request
        // Check that the final response is valid, ie used the second response
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    if (request.url.toString() == "$url$ARENA_ORDS_TOKEN_PATH?grant_type=client_credentials") {
                        respond(
                            defaultObjectMapper.writeValueAsString(token),
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        )
                    } else if (request.url.toString() == "$url/api/v1/app/skrivemodus" && count == 0) {
                        count++
                        respondError(HttpStatusCode.Unauthorized)
                    } else if (request.url.toString() == "$url/api/v1/app/skrivemodus" && count == 1) {
                        respond(
                            defaultObjectMapper.writeValueAsString(ArenaOrdsSkrivemodus(true)),
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        )
                    } else {
                        respondError(HttpStatusCode.BadRequest)
                    }
                }
            }
        }
        val arenaOrdsService = ArenaOrdsService(client, env)

        runBlocking {
            val actualResponse = arenaOrdsService.hentSkrivemodus()

            assertEquals(true, actualResponse.skrivemodus)
        }
    }
}
