package no.nav.meldeplikt.meldekortservice.api

import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
class InternalKtTest {
    @Test
    fun `test isready, isalive, og ping`() {
        val flywayConfig = mockk<Flyway>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/internal/isReady") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/meldekortservice/internal/isAlive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/meldekortservice/internal/ping") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        assertEquals("""{"ping": "pong"}""", response.content)
                    }
                }
            }
        }
    }
}
