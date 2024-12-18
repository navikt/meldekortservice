package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.mockk.coEvery
import no.nav.meldeplikt.meldekortservice.config.DUMMY_FNR
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MeldekortKtTest : TestBase() {

    @Test
    fun `get meldekortdetaljer returns ok with valid JWT`() = setUpTestApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = DUMMY_FNR,
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)


        val response = client.get("/meldekortservice/api/meldekort?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Meldekortdetaljer>(response.bodyAsText())
        assertEquals(meldekortdetaljer.id, responseObject.id)
    }

    @Test
    fun `get meldekortdetaljer returns Bad request with invalid fnr`() = setUpTestApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "12345678910",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        val response = client.get("/meldekortservice/api/meldekort?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<ErrorMessage>(response.bodyAsText())
        assertEquals(
            "Personidentifikator matcher ikke. Bruker kan derfor ikke hente ut meldekortdetaljer.", responseObject.error
        )
    }

    @Test
    fun `get meldekortdetaljer returns 401-Unauthorized with missing JWT`() = setUpTestApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = DUMMY_FNR,
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        val response = client.get("/meldekortservice/api/meldekort?meldekortId=${id}")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get korrigert meldekortid returns 401-Unauthorized with invalid JWT`() = setUpTestApplication {
        val id: Long = 1
        val nyId: Long = 123

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)

        val response = client.get("/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer Token AD")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get korrigert meldekortid returns 400 Bad Request with invalid ident`() = setUpTestApplication {
        val id: Long = 1
        val nyId: Long = 123
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312346",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)
        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        val response = client.get("/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `get meldekortdetaljer returns 401-Unauthorized with invalid JWT`() = setUpTestApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = DUMMY_FNR,
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        val response = client.get("/meldekortservice/api/meldekort?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer Token AD")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get korrigert meldekortid returns OK with valid JWT with sub`() = setUpTestApplication {
        val id: Long = 1
        val nyId: Long = 123
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = DUMMY_FNR,
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)
        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        val response = client.get("/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Long>(response.bodyAsText())
        assertEquals(nyId, responseObject)
    }

    @Test
    fun `get korrigert meldekortid returns OK with valid JWT with pid`() = setUpTestApplication {
        val id: Long = 1
        val nyId: Long = 123
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = DUMMY_FNR,
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)
        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        val response = client.get("/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Long>(response.bodyAsText())
        assertEquals(nyId, responseObject)
    }
}
