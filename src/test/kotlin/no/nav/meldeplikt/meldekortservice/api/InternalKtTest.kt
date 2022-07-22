package no.nav.meldeplikt.meldekortservice.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.locations.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import no.nav.meldeplikt.meldekortservice.config.mainModule
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@KtorExperimentalLocationsAPI
class InternalKtTest : TestBase() {

    @Test
    fun testInternal() = testApplication {
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }

        val response1 = client.get("/meldekortservice/internal/isReady")
        assertEquals(HttpStatusCode.OK, response1.status)
        assertEquals("Ready", response1.bodyAsText())

        val response2 = client.get("/meldekortservice/internal/isAlive")
        assertEquals(HttpStatusCode.OK, response2.status)
        assertEquals("Alive", response2.bodyAsText())

        val response3 = client.get("/meldekortservice/internal/ping")
        assertEquals(HttpStatusCode.OK, response3.status)
        assertEquals("""{"ping": "pong"}""", response3.bodyAsText())

        val response4 = client.get("/meldekortservice/internal/metrics")
        assertEquals(HttpStatusCode.OK, response4.status)
    }
}
