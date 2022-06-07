package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.DUMMY_TOKEN
import no.nav.meldeplikt.meldekortservice.model.feil.OrdsException
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ArenaOrdsServiceTest {
    private val fnr = "1111111111"

    @BeforeAll
    fun setup() {
        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
    }

    @Test
    fun `test hente meldekort returns OK status`() {
        val response = OrdsStringResponse(HttpStatusCode.OK, "test")
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    if (request.url.encodedPath.contains("/api/v1/meldeplikt/meldekort")
                        && request.url.host.contains("dummyurl.nav.no")
                    ) {
                        respond(
                            ObjectMapper().writeValueAsString(response),
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
    fun `test hente meldekort throws Exception`() {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    if (request.url.encodedPath.contains("/api/v1/meldeplikt/meldekort/12345678")
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

    @Test
    fun `test hent historiskeMeldekort returns OK status`() {
        val xmlString = """<Person><personId>1</personId><Etternavn>test</Etternavn><Fornavn>test</Fornavn><Maalformkode>test</Maalformkode><Meldeform>test</Meldeform><meldekortListe/><antallGjenstaaendeFeriedager>10</antallGjenstaaendeFeriedager><fravaerListe/></Person>"""
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    request.method shouldBe HttpMethod.Get
                    request.headers["Authorization"] shouldNotBe null
                    request.headers["Authorization"] shouldStartWith "Bearer $DUMMY_TOKEN"
                    request.url.toString() shouldBe "https://dummyurl.nav.no/api/v1/meldeplikt/meldekort/historiske?fnr=1234&antMeldeperioder=10"
                    respond(
                        xmlString
                    )
                }
            }
        }
        val arenaOrdsService = ArenaOrdsService(client)

        runBlocking {
            val actualResponse = arenaOrdsService.hentHistoriskeMeldekort("1234", 10)

            assertEquals(1, actualResponse.personId)
        }
    }

    @Test
    fun `test hent Meldekortdetaljer returns OK status`() {
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
                    request.method shouldBe HttpMethod.Get
                    request.headers["Authorization"] shouldNotBe null
                    request.headers["Authorization"] shouldStartWith "Bearer $DUMMY_TOKEN"
                    request.url.toString() shouldBe "https://dummyurl.nav.no/api/v1/meldeplikt/meldekort/detaljer?meldekortId=1"
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

    @Test
    fun `test kopierMeldekort returns OK status`() {
        val xmlString = """<KopierMeldekortResponse><meldekortId>123</meldekortId></KopierMeldekortResponse>"""
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    request.method shouldBe HttpMethod.Post
                    request.headers["Authorization"] shouldNotBe null
                    request.headers["Authorization"] shouldStartWith "Bearer $DUMMY_TOKEN"
                    request.headers["meldekortId"] shouldBe "123"
                    request.url.toString() shouldBe "https://dummyurl.nav.no/api/v1/meldeplikt/meldekort/kopi"
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

}