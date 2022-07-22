package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.locations.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.service.KontrollService
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
class MeldekortKtTest {

    private fun setOidcConfig(): MapApplicationConfig {
        return MapApplicationConfig(
            "ktor.environment" to "test",
            "no.nav.security.jwt.issuers.size" to "1",
            "no.nav.security.jwt.issuers.0.issuer_name" to ISSUER_ID,
            "no.nav.security.jwt.issuers.0.discoveryurl" to mockOAuth2Server.wellKnownUrl(ISSUER_ID).toString(),
            "no.nav.security.jwt.issuers.0.accepted_audience" to REQUIRED_AUDIENCE,
            "ktor.environment" to "local"
        )
    }

    private fun issueTokenWithSub(): String = mockOAuth2Server.issueToken(
        ISSUER_ID,
        "myclient",
        DefaultOAuth2TokenCallback(
            audience = listOf(REQUIRED_AUDIENCE),
            claims = mapOf("sub" to "01020312345")
        )
    ).serialize()

    private fun issueTokenWithPid(): String = mockOAuth2Server.issueToken(
        ISSUER_ID,
        "myclient",
        DefaultOAuth2TokenCallback(
            audience = listOf(REQUIRED_AUDIENCE),
            claims = mapOf("pid" to "01020312345")
        )
    ).serialize()

    companion object {
        private const val ISSUER_ID = "default"
        private const val REQUIRED_AUDIENCE = "default"

        private val mockOAuth2Server = MockOAuth2Server()

        private val env = Environment(dokarkivResendInterval = 0L)

        private var dbService = mockk<DBService>()
        private var arenaOrdsService = mockk<ArenaOrdsService>()
        private var kontrollService = mockk<KontrollService>()
        private var dokarkivService = mockk<DokarkivService>()
        private var flywayConfig = mockk<Flyway>()

        @BeforeAll
        @JvmStatic
        fun setup() {
            mockOAuth2Server.start(8091)
            every { flywayConfig.migrate() } returns MigrateResult("", "", "")

            mockkStatic(::isCurrentlyRunningOnNais)
            every { isCurrentlyRunningOnNais() } returns true
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            mockOAuth2Server.shutdown()
        }
    }

    @Test
    fun `get meldekortdetaljer returns ok with valid JWT`() = testApplication {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
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

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
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

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
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

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
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

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
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

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
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

        environment {
            config = setOidcConfig()
        }
        application {
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
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
