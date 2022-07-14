package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.locations.*
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
import org.amshove.kluent.shouldBe
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
class MeldekortKtTest {
    private fun MapApplicationConfig.setOidcConfig() {
        put("no.nav.security.jwt.issuers.size", "1")
        put("no.nav.security.jwt.issuers.0.issuer_name", ISSUER_ID)
        put("no.nav.security.jwt.issuers.0.discoveryurl", mockOAuth2Server.wellKnownUrl(ISSUER_ID).toString())
        put("no.nav.security.jwt.issuers.0.accepted_audience", REQUIRED_AUDIENCE)
        put("ktor.environment", "local")
    }

    private fun issueTokenWithSub(): String =
        mockOAuth2Server.issueToken(
            ISSUER_ID,
            "myclient",
            DefaultOAuth2TokenCallback(
                audience = listOf(REQUIRED_AUDIENCE),
                claims = mapOf(
                    "sub" to "01020312345"
                )
            )
        ).serialize()

    private fun issueTokenWithPid(): String =
        mockOAuth2Server.issueToken(
            ISSUER_ID,
            "myclient",
            DefaultOAuth2TokenCallback(
                audience = listOf(REQUIRED_AUDIENCE),
                claims = mapOf(
                    "pid" to "01020312345"
                )
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

            System.setProperty("NAIS_APP_NAME", "TEST")
            System.setProperty("NAIS_NAMESPACE", "TEST")
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            mockOAuth2Server.shutdown()
        }
    }

    @Test
    fun `get meldekortdetaljer returns ok with valid JWT`() {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
            }.apply {
                assertNotNull(response.content)
                val responseObject = defaultObjectMapper.readValue<Meldekortdetaljer>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                assertEquals(meldekortdetaljer.id, responseObject.id)
            }
        }
    }

    @Test
    fun `get meldekortdetaljer returns Bad request with invalid fnr`() {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "12345678910",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
            }.apply {
                assertNotNull(response.content)
                val responseObject = defaultObjectMapper.readValue<ErrorMessage>(response.content!!)
                response.status() shouldBe HttpStatusCode.BadRequest
                assertEquals(
                    "Personidentifikator matcher ikke. Bruker kan derfor ikke hente ut meldekortdetaljer.",
                    responseObject.error
                )
            }
        }
    }

    @Test
    fun `get meldekortdetaljer returns 401-Unauthorized with missing JWT`() {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort?meldekortId=${id}") {
            }.apply {
                response.status() shouldBe HttpStatusCode.Unauthorized
            }
        }
    }

    @Test
    fun `get korrigert meldekortid returns 401-Unauthorized with invalid JWT`() {
        val id: Long = 1
        val nyId: Long = 123

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer Token AD")
            }.apply {
                response.status() shouldBe HttpStatusCode.Unauthorized
            }
        }
    }

    @Test
    fun `get meldekortdetaljer returns 401-Unauthorized with invalid JWT`() {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(
            id = "1",
            fodselsnr = "01020312345",
            kortType = KortType.AAP
        )

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer Token AD")
            }.apply {
                response.status() shouldBe HttpStatusCode.Unauthorized
            }
        }
    }

    @Test
    fun `get korrigert meldekortid returns OK with valid JWT with sub`() {
        val id: Long = 1
        val nyId: Long = 123

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueTokenWithSub()}")
            }.apply {
                assertNotNull(response.content)
                val responseObject = defaultObjectMapper.readValue<Long>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                assertEquals(nyId, responseObject)
            }
        }
    }

    @Test
    fun `get korrigert meldekortid returns OK with valid JWT with pid`() {
        val id: Long = 1
        val nyId: Long = 123

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(
                env = env,
                mockDBService = dbService,
                arenaOrdsService = arenaOrdsService,
                kontrollService = kontrollService,
                dokarkivService = dokarkivService,
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueTokenWithPid()}")
            }.apply {
                assertNotNull(response.content)
                val responseObject = defaultObjectMapper.readValue<Long>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                assertEquals(nyId, responseObject)
            }
        }
    }
}