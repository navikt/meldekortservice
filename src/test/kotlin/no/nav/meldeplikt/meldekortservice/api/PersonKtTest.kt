package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.locations.*
import io.ktor.server.testing.*
import io.mockk.*
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.database.hentMidlertidigLagredeJournalposter
import no.nav.meldeplikt.meldekortservice.model.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.DokumentInfo
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.JournalpostResponse
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekort.Meldekort
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.defaultXmlMapper
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@KtorExperimentalLocationsAPI
class PersonKtTest : TestBase() {

    @Test
    fun `get historiske meldekort returns ok with valid JWT`() = testApplication {
        val period = 1
        val fnr = "01020312345"
        val person = Person(1L, "Bob", "Kåre", "No", "Papp", listOf(), 10, listOf())

        coEvery { arenaOrdsService.hentHistoriskeMeldekort(fnr, period) } returns (person)
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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

        val response = client.get("/meldekortservice/api/person/historiskemeldekort?antallMeldeperioder=${period}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Person>(response.bodyAsText())
        assertEquals(person.personId, responseObject.personId)
    }

    @Test
    fun `get historiske meldekort returns 401-Unauthorized with missing JWT`() = testApplication {
        val period = 1
        val fnr = "01020312345"
        val person = Person(1L, "Bob", "Kåre", "No", "Papp", listOf(), 10, listOf())

        coEvery { arenaOrdsService.hentHistoriskeMeldekort(fnr, period) } returns (person)
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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

        val response = client.get("/meldekortservice/api/person/historiskemeldekort?antallMeldeperioder=${period}")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get person meldekort returns ok with valid JWT`() = testApplication {
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
        coEvery { dbService.hentInnsendtMeldekort(1L) } returns (InnsendtMeldekort(meldekortId = 1L))
        coEvery { dbService.hentInnsendtMeldekort(2L) } throws SQLException("Found no rows")
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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

        val response = client.get("/meldekortservice/api/person/meldekort") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Person>(response.bodyAsText())
        assertEquals(person.personId, responseObject.personId)
        assertEquals(1, responseObject.meldekortListe?.size)
    }

    @Test
    fun `get person meldekort returns NoContent status when no response from ORDS`() = testApplication {
        val ordsStringResponse = OrdsStringResponse(status = HttpStatusCode.BadRequest, content = "")

        coEvery { arenaOrdsService.hentMeldekort(any()) } returns (ordsStringResponse)
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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

        val response = client.get("/meldekortservice/api/person/meldekort") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `Kontroll or innsending of meldekort returns OK`() = testApplication {
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP,
            meldeperiode = "20200105",
            sporsmal = Sporsmal(meldekortDager = listOf())
        )

        val meldekortKontrollertType = MeldekortKontrollertType()
        meldekortKontrollertType.meldekortId = 1L
        meldekortKontrollertType.status = "OK"
        meldekortKontrollertType.arsakskoder = MeldekortKontrollertType.Arsakskoder()

        coEvery { dbService.settInnInnsendtMeldekort(any()) } just Runs
        coEvery { kontrollService.kontroller(any()) } returns meldekortKontrollertType
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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

        val response = client.post("/meldekortservice/api/person/meldekort") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(defaultObjectMapper.writeValueAsString(meldekortdetaljer))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<MeldekortKontrollertType>(response.bodyAsText())
        assertEquals(meldekortKontrollertType.meldekortId, responseObject.meldekortId)
    }

    @Test
    fun `Kontroll of meldekort returns ServiceUnavailable`() = testApplication {
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP,
            meldeperiode = "20200105",
            sporsmal = Sporsmal(meldekortDager = listOf())
        )
        val meldekortKontrollertType = MeldekortKontrollertType()
        meldekortKontrollertType.meldekortId = 1L
        meldekortKontrollertType.status = "OK"
        meldekortKontrollertType.arsakskoder = MeldekortKontrollertType.Arsakskoder()

        coEvery { dbService.settInnInnsendtMeldekort(any()) } just Runs
        coEvery { kontrollService.kontroller(any()) } throws RuntimeException("Feil i meldekortkontroll-api")
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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

        val response = client.post("/meldekortservice/api/person/meldekort") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(defaultObjectMapper.writeValueAsString(meldekortdetaljer))
        }

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
    }

    @Test
    fun `OpprettJournalpost returnerer OK hvis DokarkivService er ok`() = testApplication {
        val journalpostId = 123456780L
        val dokumentInfoId = 123456781L

        val journalpostResponse = JournalpostResponse(
            journalpostId = journalpostId,
            journalstatus = "M",
            melding = "MELDING FRA DOKARKIV",
            journalpostferdigstilt = true,
            dokumenter = listOf(
                DokumentInfo(dokumentInfoId)
            )
        )

        coEvery { dokarkivService.createJournalpost(any()) } returns journalpostResponse
        every { dbService.lagreJournalpostData(any(), any(), any()) } just Runs
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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

        val response = client.post("/meldekortservice/api/person/opprettJournalpost") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(this::class.java.getResource("/journalpost.json")!!.readText())
        }

        assertEquals(HttpStatusCode.OK, response.status)

        // MeldekortId kommer fra tilleggsopplysninger i journalpost.json
        verify { dbService.lagreJournalpostData(journalpostId, dokumentInfoId, 1011121315) }
    }

    @Test
    fun `OpprettJournalpost returnerer ServiceUnavailable hvis DokarkivService ikke er ok`() = testApplication {
        val journalpost = this::class.java.getResource("/journalpost.json")

        coEvery { dokarkivService.createJournalpost(any()) } throws Exception()
        every { dbService.lagreJournalpostMidlertidig(any()) } just Runs
        every { dbService.getConnection().hentMidlertidigLagredeJournalposter() } returns emptyList()
        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

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

        val response = client.post("/meldekortservice/api/person/opprettJournalpost") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(journalpost!!.readText())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(
            response.bodyAsText()
                .startsWith("{\"error\":\"Kan ikke opprette journalpost i dokumentarkiv med eksternReferanseId ")
        )
        // MeldekortId kommer fra tilleggsopplysninger i journalpost.json
        assertTrue(response.bodyAsText().endsWith("for meldekort med id 1011121315\"}"))

        verify {
            dbService.lagreJournalpostMidlertidig(
                jacksonObjectMapper().readValue(
                    journalpost,
                    Journalpost::class.java
                )
            )
        }
    }
}
