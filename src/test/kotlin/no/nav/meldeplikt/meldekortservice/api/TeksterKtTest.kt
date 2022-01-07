package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TeksterKtTest {
    @Test
    fun `skal returnere id hvis eksisterer`() {
        val id = "some.id-AAP"
        val language = "nb"
        val from = "0000-00-00T00:00:00"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentTekst(id, language, from) } returns id

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
                "/meldekortservice/api/tekster/eksisterer?id=${id}&language=${language}&from=${from}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(id, response.content)
            }
        }
    }

    @Test
    fun `skal returnere forkortet id hvis ikke eksisterer`() {
        val forkortetId = "some.id"
        val id = "$forkortetId-AAP"
        val language = "nb"
        val from = "0000-00-00T00:00:00"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentTekst(id, language, from) } returns null

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
                "/meldekortservice/api/tekster/eksisterer?id=${id}&language=${language}&from=${from}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(forkortetId, response.content)
            }
        }
    }

    @Test
    fun `skal returnere tekst hvis eksisterer`() {
        val id = "some.id"
        val language = "nb"
        val from = "0000-00-00T00:00:00"
        val tekst = "Some text"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentTekst(id, language, from) } returns tekst

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
                "/meldekortservice/api/tekster/hent?id=${id}&language=${language}&from=${from}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(tekst, response.content)
            }
        }
    }

    @Test
    fun `skal returnere id hvis tekst ikke eksisterer`() {
        val id = "some.id-AAP"
        val language = "nb"
        val from = "0000-00-00T00:00:00"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentTekst(id, language, from) } returns null

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
                "/meldekortservice/api/tekster/hent?id=${id}&language=${language}&from=${from}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(id, response.content)
            }
        }
    }

    @Test
    fun `skal returnere tekster`() {
        val language = "nb"
        val from = "0000-00-00T00:00:00"
        val tekster = mutableMapOf<String, String>()
        tekster["key1"] = "value1"
        tekster["key2"] = "value2"
        tekster["key3"] = "value3"

        val flywayConfig = mockk<Flyway>()
        val dbService = mockk<DBService>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { dbService.hentAlleTekster(language, from) } returns tekster

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
                "/meldekortservice/api/tekster/hentAlle?language=${language}&from=${from}"
            ) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseObject = defaultObjectMapper.readValue<Map<String, String>>(response.content!!)
                assertEquals(tekster, responseObject)
            }
        }
    }
}