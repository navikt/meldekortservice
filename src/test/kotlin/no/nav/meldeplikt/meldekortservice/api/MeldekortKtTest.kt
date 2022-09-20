package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.locations.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
class MeldekortKtTest : TestBase() {

    @Test
    fun `get meldekortdetaljer returns ok with valid JWT`() = testApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)
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

        val response = client.get("/meldekortservice/api/meldekort?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Meldekortdetaljer>(response.bodyAsText())
        assertEquals(meldekortdetaljer.id, responseObject.id)
    }

    @Test
    fun `get meldekortdetaljer returns Bad request with invalid fnr`() = testApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "12345678910",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)
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
    fun `get meldekortdetaljer returns 401-Unauthorized with missing JWT`() = testApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)
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

        val response = client.get("/meldekortservice/api/meldekort?meldekortId=${id}")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get korrigert meldekortid returns 401-Unauthorized with invalid JWT`() = testApplication {
        val id: Long = 1
        val nyId: Long = 123

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)
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

        val response = client.get("/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer Token AD")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get meldekortdetaljer returns 401-Unauthorized with invalid JWT`() = testApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)
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

        val response = client.get("/meldekortservice/api/meldekort?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer Token AD")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get korrigert meldekortid returns OK with valid JWT with sub`() = testApplication {
        val id: Long = 1
        val nyId: Long = 123

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)
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

        val response = client.get("/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Long>(response.bodyAsText())
        assertEquals(nyId, responseObject)
    }

    @Test
    fun `get korrigert meldekortid returns OK with valid JWT with pid`() = testApplication {
        val id: Long = 1
        val nyId: Long = 123

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)
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

        val response = client.get("/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
            header(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<Long>(response.bodyAsText())
        assertEquals(nyId, responseObject)
    }
}
