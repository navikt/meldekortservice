package no.nav.meldeplikt.meldekortservice

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import io.netty.handler.codec.http.HttpHeaders.addHeader
import org.junit.jupiter.api.Test
import io.ktor.server.testing.withTestApplication
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.mockk.coEvery
import io.mockk.every
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.mainModule
import org.junit.jupiter.api.BeforeAll
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.ServerMock
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import org.amshove.kluent.shouldBe


class ApplicationTest {
    @BeforeAll
    fun before() {
        System.setProperty("KONTROLL_CLIENT_ID", "test")
        System.setProperty("AZURE_CLIENT_ID", "test")
        System.setProperty("AZURE_CLIENT_SECRET", "test")
        System.setProperty("NAIS_APP_NAME", "test")

    }

    @Test
    fun `Test health-api OK response`() {
            val id: Long = 1
            val meldekortdetaljer = Meldekortdetaljer(id = "1",
                    kortType = KortType.AAP)
            val arenaOrdsService = mockk<ArenaOrdsService>()
            coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)
            val flywayConfig = mockk<org.flywaydb.core.Flyway>()
            every { flywayConfig.migrate() } returns 0
            withTestApplication(
                    ServerMock(flywayConfig = flywayConfig,
                            arenaOrdsService = arenaOrdsService)
            ) {
                handleRequest(HttpMethod.Get, "/meldekortservice/internal/isAlive")
                        .apply {
                            response.status() shouldBe HttpStatusCode.OK
                        }
            }


    }
}