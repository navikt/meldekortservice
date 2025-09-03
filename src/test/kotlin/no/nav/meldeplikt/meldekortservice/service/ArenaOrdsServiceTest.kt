package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.module.kotlin.readValue
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
import no.nav.meldeplikt.meldekortservice.model.meldegruppe.Meldegruppe
import no.nav.meldeplikt.meldekortservice.model.meldegruppe.MeldegruppeResponse
import no.nav.meldeplikt.meldekortservice.model.meldestatus.Endring
import no.nav.meldeplikt.meldekortservice.model.meldestatus.Meldeplikt
import no.nav.meldeplikt.meldekortservice.model.meldestatus.MeldestatusResponse
import no.nav.meldeplikt.meldekortservice.model.meldestatus.Periode
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.ARENA_ORDS_HENT_MELDEGRUPPER
import no.nav.meldeplikt.meldekortservice.utils.ARENA_ORDS_TOKEN_PATH
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ArenaOrdsServiceTest {
    private val fnr = "1111111111"

    @BeforeAll
    fun setup() {
        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
    }

    @Nested
    inner class HentMeldekort {
        @Test
        fun `hentMeldekort returnerer OK status`() {
            val response = OrdsStringResponse(HttpStatusCode.OK, "test")
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        if (request.url.toString() == "https://dummyurl.nav.no/api/v2/meldeplikt/meldekort") {
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                            assertEquals(fnr, request.headers["fnr"])

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
                val actualResponse = arenaOrdsService.hentMeldekort(fnr)
                val ordsResponse: OrdsStringResponse = defaultObjectMapper.readValue(actualResponse.content)

                assertEquals(HttpStatusCode.OK, actualResponse.status)
                assertEquals(response.content, ordsResponse.content)
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
            assertEquals("Kunne ikke hente meldekort fra Arena Ords.", exception.localizedMessage)
        }
    }

    @Nested
    inner class HentHistoriskeMeldekort {
        @Test
        fun `hentHistoriskeMeldekort returnerer OK status`() {
            val xmlString =
                """<Person><personId>1</personId><Etternavn>test</Etternavn><Fornavn>test</Fornavn><Maalformkode>test</Maalformkode><Meldeform>test</Meldeform><meldekortListe/><antallGjenstaaendeFeriedager>10</antallGjenstaaendeFeriedager><fravaerListe/></Person>"""
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
                            xmlString
                        )
                    }
                }
            }
            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentHistoriskeMeldekort(fnr, 10)

                assertEquals(1, actualResponse.personId)
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
    inner class HentMeldegrupper {
        @Test
        fun `hentMeldegrupper returnerer data`() {
            val meldegruppeResponse = MeldegruppeResponse(
                listOf(
                    Meldegruppe(
                        "",
                        "ARBS",
                        LocalDate.now(),
                        null,
                        LocalDate.now(),
                        "J",
                        "",
                        null
                    ),
                    Meldegruppe(
                        "",
                        "ARBS",
                        LocalDate.now(),
                        LocalDate.now(),
                        LocalDate.now(),
                        "J",
                        null,
                        null
                    )
                )
            )

            val personId = "1019108"
            val person = "" +
                    "<Person>" +
                    "    <PersonId>$personId</PersonId>" +
                    "    <Etternavn>DUCK</Etternavn>" +
                    "    <Fornavn>DONALD</Fornavn>" +
                    "    <Maalformkode>NO</Maalformkode>" +
                    "    <Meldeform>EMELD</Meldeform>" +
                    "    <MeldekortListe/>" +
                    "    <FravaerListe/>" +
                    "</Person>"

            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        if (request.url.toString() == "https://dummyurl.nav.no/api/v2/meldeplikt/meldekort") {
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                            assertEquals(fnr, request.headers["fnr"])

                            respond(
                                person,
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Xml.toString())
                            )
                        } else {
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                            assertEquals(personId, request.headers["personid"])
                            assertEquals(
                                "https://dummyurl.nav.no$ARENA_ORDS_HENT_MELDEGRUPPER",
                                request.url.toString()
                            )

                            respond(
                                defaultObjectMapper.writeValueAsString(meldegruppeResponse),
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            )
                        }
                    }
                }
            }

            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentMeldegrupper(fnr, LocalDate.now())
                assertEquals(meldegruppeResponse, actualResponse)
            }
        }

        @Test
        fun `hentMeldegrupper returnerer emptyList for brukere uten meldpelikt`() {
            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { _ ->
                        respond("", HttpStatusCode.NoContent)
                    }
                }
            }

            val arenaOrdsService = ArenaOrdsService(client)

            runBlocking {
                val actualResponse = arenaOrdsService.hentMeldegrupper(fnr, LocalDate.now())
                assertEquals(emptyList(), actualResponse.meldegruppeListe)
            }
        }

        @Test
        fun `hentMeldegrupper kaster Exception når ikke kan hente meldegrupper fra Arena ORDS`() {
            val personId = "1019108"
            val person = "" +
                    "<Person>" +
                    "    <PersonId>$personId</PersonId>" +
                    "    <Etternavn>DUCK</Etternavn>" +
                    "    <Fornavn>DONALD</Fornavn>" +
                    "    <Maalformkode>NO</Maalformkode>" +
                    "    <Meldeform>EMELD</Meldeform>" +
                    "    <MeldekortListe/>" +
                    "    <FravaerListe/>" +
                    "</Person>"

            val client = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        if (request.url.toString() == "https://dummyurl.nav.no/api/v2/meldeplikt/meldekort") {
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                            assertEquals(fnr, request.headers["fnr"])

                            respond(
                                person,
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Xml.toString())
                            )
                        } else {
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("Bearer $DUMMY_TOKEN", request.headers["Authorization"])
                            assertEquals(personId, request.headers["personid"])
                            assertEquals(
                                "https://dummyurl.nav.no$ARENA_ORDS_HENT_MELDEGRUPPER",
                                request.url.toString()
                            )

                            respond("", HttpStatusCode.NoContent)
                        }
                    }
                }
            }

            val arenaOrdsService = ArenaOrdsService(client)

            val exception = assertThrows<OrdsException> {
                runBlocking {
                    arenaOrdsService.hentMeldegrupper(fnr, LocalDate.now())
                }
            }
            assertEquals("Kunne ikke hente meldegrupper fra Arena Ords", exception.localizedMessage)
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
        // Check that final response is valid, ie used the second response
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
