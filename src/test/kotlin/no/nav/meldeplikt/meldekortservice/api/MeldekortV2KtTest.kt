package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.locations.*
import io.mockk.coEvery
import no.nav.meldeplikt.meldekortservice.config.DUMMY_FNR
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekort.Meldekort
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.defaultXmlMapper
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
class MeldekortV2KtTest : TestBase() {

    private val hentMeldekortUrl = "/meldekortservice/api/v2/meldekort"
    private val hentHistoriskeMeldekortUrl = "/meldekortservice/api/v2/historiskemeldekort?antallMeldeperioder=1"
    private val hentMeldekortdetaljer = "/meldekortservice/api/v2/meldekortdetaljer?meldekortId=123456789"
    private val hentKorrigertMeldekort = "/meldekortservice/api/v2/korrigertMeldekort?meldekortId=123456789"

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
        assertNotNull(response.bodyAsText())
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
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Person>(response.bodyAsText())
        assertEquals(person.personId, responseObject.personId)
    }

    @Test
    fun `hentMeldekortdetaljer returns Unauthorized status when no token in headers`() = setUpTestApplication {
        val response = client.get(hentMeldekortdetaljer) {
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `hentMeldekortdetaljer returns BadRequest status when no ident in Headers`() = setUpTestApplication {
        val response = client.get(hentMeldekortdetaljer) {
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


        val response = client.get(hentMeldekortdetaljer) {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
            header("ident", DUMMY_FNR)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Meldekortdetaljer>(response.bodyAsText())
        assertEquals(meldekortdetaljer.id, responseObject.id)
    }

    @Test
    fun `hentKorrigertMeldekort returns Unauthorized status when no token in headers`() = setUpTestApplication {
        val response = client.get(hentKorrigertMeldekort) {
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `hentKorrigertMeldekort returns BadRequest status when no ident in Headers`() = setUpTestApplication {
        val response = client.get(hentKorrigertMeldekort) {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `hentKorrigertMeldekort returns BadRequest status when valid token and ident in headers but wrong ident`() = setUpTestApplication {
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = DUMMY_FNR,
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        val response = client.get(hentKorrigertMeldekort) {
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

        val response = client.get(hentKorrigertMeldekort) {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            header("ident", DUMMY_FNR)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Long>(response.bodyAsText())
        assertEquals(nyId, responseObject)
    }
}
