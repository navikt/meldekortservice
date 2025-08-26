package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import no.nav.meldeplikt.meldekortservice.config.DUMMY_FNR
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldegruppe.Meldegruppe
import no.nav.meldeplikt.meldekortservice.model.meldegruppe.MeldegruppeResponse
import no.nav.meldeplikt.meldekortservice.model.meldekort.Meldekort
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldestatus.*
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.defaultXmlMapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class MeldekortV2KtTest : TestBase() {

    private val hentMeldekortUrl = "/meldekortservice/api/v2/meldekort"
    private val hentHistoriskeMeldekortUrl = "/meldekortservice/api/v2/historiskemeldekort?antallMeldeperioder=1"
    private val hentMeldekortdetaljerUrl = "/meldekortservice/api/v2/meldekortdetaljer?meldekortId=123456789"
    private val hentKorrigertMeldekortUrl = "/meldekortservice/api/v2/korrigertMeldekort?meldekortId=123456789"
    private val hentMeldegrupperUrl = "/meldekortservice/api/v2/meldegrupper"
    private val hentMeldestatusUrl = "/meldekortservice/api/v2/meldestatus"

    @Nested
    inner class HentMeldekort {
        @Test
        fun `hentMeldekort returns Unauthorized status when no token in headers`() = setUpTestApplication {
            val response = client.get(hentMeldekortUrl) {
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `hentMeldekort returns BadRequest status when no ident in Headers`() = setUpTestApplication {
            val response = client.get(hentMeldekortUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `hentMeldekort returns data when valid token and ident in headers`() = setUpTestApplication {
            val meldekort1 = Meldekort(
                1L,
                KortType.MASKINELT_OPPDATERT.code,
                "201920",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "DAGP",
                "Ferdig",
                false,
                LocalDate.now().minusDays(1),
                3F
            )
            val meldekort2 = Meldekort(
                2L,
                KortType.MASKINELT_OPPDATERT.code,
                "201920",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "DAGP",
                "Ferdig",
                false,
                LocalDate.now().minusDays(1),
                3F
            )
            val person = Person(
                1L,
                "Bob",
                "Kåre",
                "No",
                "Papp",
                listOf(meldekort1, meldekort2),
                10,
                listOf()
            )
            val ordsStringResponse = OrdsStringResponse(
                status = HttpStatusCode.OK,
                content = defaultXmlMapper.writeValueAsString(person)
            )

            coEvery { arenaOrdsService.hentMeldekort(any()) } returns (ordsStringResponse)

            val response = client.get(hentMeldekortUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                header("ident", DUMMY_FNR)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseObject = defaultObjectMapper.readValue<Person>(response.bodyAsText())
            assertEquals(person.personId, responseObject.personId)
            assertEquals(2, responseObject.meldekortListe?.size)
        }

        @Test
        fun `hentMeldekort returns NoContent status when no response from ORDS`() = setUpTestApplication {
            val ordsStringResponse = OrdsStringResponse(status = HttpStatusCode.BadRequest, content = "")

            coEvery { arenaOrdsService.hentMeldekort(any()) } returns (ordsStringResponse)

            val response = client.get(hentMeldekortUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                header("ident", DUMMY_FNR)
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }
    }

    @Nested
    inner class HentHistoriskeMeldekort {
        @Test
        fun `hentHistoriskeMeldekort returns Unauthorized status when no token in headers`() = setUpTestApplication {
            val response = client.get(hentHistoriskeMeldekortUrl) {
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `hentHistoriskeMeldekort returns BadRequest status when no ident in headers`() = setUpTestApplication {
            val response = client.get(hentHistoriskeMeldekortUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `hentHistoriskeMeldekort returns data when valid token and ident in headers`() = setUpTestApplication {
            val person = Person(1L, "Bob", "Kåre", "No", "Papp", listOf(), 10, listOf())

            coEvery { arenaOrdsService.hentHistoriskeMeldekort(DUMMY_FNR, 1) } returns (person)

            val response = client.get(hentHistoriskeMeldekortUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                header("ident", DUMMY_FNR)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseObject = defaultObjectMapper.readValue<Person>(response.bodyAsText())
            assertEquals(person.personId, responseObject.personId)
        }
    }

    @Nested
    inner class HentMeldekortdetaljer {
        @Test
        fun `hentMeldekortdetaljer returns Unauthorized status when no token in headers`() = setUpTestApplication {
            val response = client.get(hentMeldekortdetaljerUrl) {
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `hentMeldekortdetaljer returns BadRequest status when no ident in Headers`() = setUpTestApplication {
            val response = client.get(hentMeldekortdetaljerUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `hentMeldekortdetaljer returns data when valid token and ident in headers`() = setUpTestApplication {
            val meldekortdetaljer = Meldekortdetaljer(
                id = "1",
                fodselsnr = DUMMY_FNR,
                kortType = KortType.AAP
            )

            coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)


            val response = client.get(hentMeldekortdetaljerUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
                header("ident", DUMMY_FNR)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseObject = defaultObjectMapper.readValue<Meldekortdetaljer>(response.bodyAsText())
            assertEquals(meldekortdetaljer.id, responseObject.id)
        }
    }

    @Nested
    inner class HentKorrigertMeldekort {
        @Test
        fun `hentKorrigertMeldekort returns Unauthorized status when no token in headers`() = setUpTestApplication {
            val response = client.get(hentKorrigertMeldekortUrl) {
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `hentKorrigertMeldekort returns BadRequest status when no ident in Headers`() = setUpTestApplication {
            val response = client.get(hentKorrigertMeldekortUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `hentKorrigertMeldekort returns BadRequest status when valid token and ident in headers but wrong ident`() =
            setUpTestApplication {
                val meldekortdetaljer = Meldekortdetaljer(
                    id = "1",
                    fodselsnr = DUMMY_FNR,
                    kortType = KortType.AAP
                )

                coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

                val response = client.get(hentKorrigertMeldekortUrl) {
                    header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                    header("ident", "21020312345")
                }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `hentKorrigertMeldekort returns data when valid token and ident in headers`() = setUpTestApplication {
            val meldekortdetaljer = Meldekortdetaljer(
                id = "1",
                fodselsnr = DUMMY_FNR,
                kortType = KortType.AAP
            )
            val nyId: Long = 223456789

            coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)
            coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)

            val response = client.get(hentKorrigertMeldekortUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                header("ident", DUMMY_FNR)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseObject = defaultObjectMapper.readValue<Long>(response.bodyAsText())
            assertEquals(nyId, responseObject)
        }
    }

    @Nested
    inner class HentMeldegrupper {
        @Test
        fun `hentMeldegrupper returns Unauthorized status when no token in headers`() = setUpTestApplication {
            val response = client.get(hentMeldegrupperUrl) {
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `hentMeldegrupper returns BadRequest status when no ident in Headers`() = setUpTestApplication {
            val response = client.get(hentMeldegrupperUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `hentMeldegrupper returns data when valid token and ident in headers`() = setUpTestApplication {
            val meldegrupper = listOf(
                Meldegruppe(
                    DUMMY_FNR,
                    "ARBS",
                    LocalDate.now(),
                    null,
                    LocalDate.now(),
                    "J",
                    "Aktivert med ingen ytelser",
                    null
                ),
                Meldegruppe(
                    DUMMY_FNR,
                    "DAGP",
                    LocalDate.now(),
                    LocalDate.now(),
                    LocalDate.now(),
                    "J",
                    "Iverksatt vedtak",
                    1L
                )
            )
            val meldegruppeResponse = MeldegruppeResponse(meldegrupper)

            coEvery { arenaOrdsService.hentMeldegrupper(any(), any()) } returns (meldegruppeResponse)

            val response = client.get(hentMeldegrupperUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                header("ident", DUMMY_FNR)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseObject = defaultObjectMapper.readValue<List<Meldegruppe>>(response.bodyAsText())
            assertEquals(meldegrupper, responseObject)
        }

        @Test
        fun `hentMeldegrupper returns emptyList when meldegruppeListe is null`() = setUpTestApplication {
            val meldegruppeResponse = MeldegruppeResponse(null)

            coEvery { arenaOrdsService.hentMeldegrupper(any(), any()) } returns (meldegruppeResponse)

            val response = client.get(hentMeldegrupperUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                header("ident", DUMMY_FNR)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseObject = defaultObjectMapper.readValue<List<Meldegruppe>>(response.bodyAsText())
            assertEquals(emptyList(), responseObject)
        }
    }

    @Nested
    inner class HentMeldestatus {
        @Test
        fun `hentMeldestatus returns Unauthorized status when no token in headers`() = setUpTestApplication {
            val response = client.post(hentMeldestatusUrl) {
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `hentMeldestatus returns BadRequest status when no post body`() = setUpTestApplication {
            val response = client.post(hentMeldestatusUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `hentMeldestatus returns data`() = setUpTestApplication {
            val arenaPersonId = 123456789L
            val sokeDato = LocalDate.now()

            val meldestatusResponse = MeldestatusResponse(
                arenaPersonId,
                DUMMY_FNR,
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
                ),
                listOf(
                    Meldegruppe(
                        "ATTF",
                        Periode(
                            LocalDateTime.now().minusDays(10),
                        ),
                        "Bla bla",
                        Endring(
                            "R123456",
                            LocalDateTime.now().minusDays(7),
                            "E654321",
                            LocalDateTime.now()
                        ),
                    )
                )
            )

            coEvery {
                arenaOrdsService.hentMeldestatus(
                    eq(arenaPersonId),
                    eq(DUMMY_FNR),
                    eq(sokeDato)
                )
            } returns (meldestatusResponse)

            val response = client.post(hentMeldestatusUrl) {
                header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                setBody(defaultObjectMapper.writeValueAsString(MeldestatusRequest(arenaPersonId, DUMMY_FNR, sokeDato)))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseObject = defaultObjectMapper.readValue<MeldestatusResponse>(response.bodyAsText())
            assertEquals(meldestatusResponse, responseObject)
        }
    }
}
