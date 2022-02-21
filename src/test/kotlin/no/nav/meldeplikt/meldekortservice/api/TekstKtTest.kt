package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TekstKtTest {

    private val database = H2Database()

    @AfterAll
    fun tearDown() {
        database.closeConnection()
    }

    @Test
    fun `skal returnere id hvis eksisterer`() {
        val kode = "test.kode-AAP"
        val sprak = "nb"
        val fraDato = "0000-00-00"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentTekst(kode, sprak, fraDato) } returns kode

        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = dbService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(
                HttpMethod.Get,
                "/meldekortservice/api/tekst/eksisterer?kode=${kode}&sprak=${sprak}&fraDato=${fraDato}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(kode, response.content)
            }
        }
    }

    @Test
    fun `skal returnere forkortet id hvis ikke eksisterer`() {
        val forkortetKode = "test.kode"
        val kode = "$forkortetKode-AAP"
        val sprak = "nb"
        val fraDato = "0000-00-00"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentTekst(kode, sprak, fraDato) } returns null

        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = dbService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(
                HttpMethod.Get,
                "/meldekortservice/api/tekst/eksisterer?kode=${kode}&sprak=${sprak}&fraDato=${fraDato}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(forkortetKode, response.content)
            }
        }
    }

    @Test
    fun `skal returnere tekst hvis eksisterer`() {
        val kode = "test.kode"
        val sprak = "nb"
        val fraDato = "0000-00-00"
        val tekst = "Bla bla bla"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentTekst(kode, sprak, fraDato) } returns tekst

        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = dbService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(
                HttpMethod.Get,
                "/meldekortservice/api/tekst/hent?kode=${kode}&sprak=${sprak}&fraDato=${fraDato}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(tekst, response.content)
            }
        }
    }

    @Test
    fun `skal returnere id hvis tekst ikke eksisterer`() {
        val kode = "test.kode-AAP"
        val sprak = "nb"
        val fraDato = "0000-00-00"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentTekst(kode, sprak, fraDato) } returns null

        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = dbService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(
                HttpMethod.Get,
                "/meldekortservice/api/tekst/hent?kode=${kode}&sprak=${sprak}&fraDato=${fraDato}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(kode, response.content)
            }
        }
    }

    @Test
    fun `skal returnere tekster`() {
        val sprak = "nb"
        val fraDato = "0000-00-00"
        val tekster = mutableMapOf<String, String>()
        tekster["kode1"] = "verdi1"
        tekster["kode2"] = "verdi2"
        tekster["kode3"] = "verdi3"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentAlleTekster(sprak, fraDato) } returns tekster

        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = dbService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(
                HttpMethod.Get,
                "/meldekortservice/api/tekst/hentAlle?sprak=${sprak}&fraDato=${fraDato}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseObject = defaultObjectMapper.readValue<Map<String, String>>(response.content!!)
                assertEquals(tekster, responseObject)
            }
        }
    }

    @Test
    fun `skal returnere tekster selv om fraDato er feil`() {
        val sprak = "nb"
        val fraDato = "0-00-00"

        val flywayConfig = mockk<Flyway>()
        val dbService = DBService(database)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")


        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = dbService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(
                HttpMethod.Get,
                "/meldekortservice/api/tekst/hentAlle?sprak=${sprak}&fraDato=${fraDato}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseObject = defaultObjectMapper.readValue<Map<String, String>>(response.content!!)
                assert(responseObject.isNotEmpty())
            }
        }
    }
}