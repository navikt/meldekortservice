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
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.defaultXmlMapper
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
class PersonKtTest : TestBase() {

    @Test
    fun `get historiske meldekort returns ok with valid JWT`() = setUpTestApplication {
        val period = 1
        val person = Person(1L, "Bob", "Kåre", "No", "Papp", listOf(), 10, listOf())

        coEvery { arenaOrdsService.hentHistoriskeMeldekort(DUMMY_FNR, period) } returns (person)

        val response = client.get("/meldekortservice/api/person/historiskemeldekort?antallMeldeperioder=${period}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Person>(response.bodyAsText())
        assertEquals(person.personId, responseObject.personId)
    }

    @Test
    fun `get historiske meldekort returns 401-Unauthorized with missing JWT`() = setUpTestApplication {
        val period = 1
        val person = Person(1L, "Bob", "Kåre", "No", "Papp", listOf(), 10, listOf())

        coEvery { arenaOrdsService.hentHistoriskeMeldekort(DUMMY_FNR, period) } returns (person)

        val response = client.get("/meldekortservice/api/person/historiskemeldekort?antallMeldeperioder=${period}")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get person meldekort returns ok with valid JWT`() = setUpTestApplication {
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

        val response = client.get("/meldekortservice/api/person/meldekort") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Person>(response.bodyAsText())
        assertEquals(person.personId, responseObject.personId)
        assertEquals(2, responseObject.meldekortListe?.size)
    }

    @Test
    fun `get person meldekort returns NoContent status when no response from ORDS`() = setUpTestApplication {
        val ordsStringResponse = OrdsStringResponse(status = HttpStatusCode.BadRequest, content = "")

        coEvery { arenaOrdsService.hentMeldekort(any()) } returns (ordsStringResponse)

        val response = client.get("/meldekortservice/api/person/meldekort") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }
}
