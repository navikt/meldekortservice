package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.amshove.kluent.shouldBe
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
class MeldekortKtTest{
    private fun MapApplicationConfig.setOidcConfig() {
        put("no.nav.security.jwt.issuers.size", "1")
        put("no.nav.security.jwt.issuers.0.issuer_name", ISSUER_ID)
        put("no.nav.security.jwt.issuers.0.discoveryurl", mockOAuth2Server.wellKnownUrl(ISSUER_ID).toString())
        put("no.nav.security.jwt.issuers.0.accepted_audience", REQUIRED_AUDIENCE)
        put("no.nav.security.jwt.required_issuer_name", ISSUER_ID)
        put("ktor.environment", "local")
    }

    private fun issueToken(): String =
        mockOAuth2Server.issueToken(
            ISSUER_ID,
            "myclient",
            DefaultOAuth2TokenCallback(
                audience = listOf(REQUIRED_AUDIENCE),
                claims = mapOf(
                    "sub" to "11111111111"
                )
            )
        ).serialize()

    companion object {
        private const val ISSUER_ID = "default"
        private const val REQUIRED_AUDIENCE = "default"

        private val mockOAuth2Server = MockOAuth2Server()
        private val flywayConfig = mockk<Flyway>()
        private val arenaOrdsService = mockk<ArenaOrdsService>()

        @BeforeAll
        @JvmStatic
        fun setup() {
            mockOAuth2Server.start(8091)
            every { flywayConfig.migrate() } returns 0
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
        val meldekortdetaljer = Meldekortdetaljer(id = "1",
            fodselsnr = "11111111111",
            kortType = KortType.AAP)

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                kontrollService = mockk(),
                mockInnsendtMeldekortService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")
            }.apply {
                val mapper = jacksonObjectMapper()
                assertNotNull(response.content)
                val responseObject = mapper.readValue<Meldekortdetaljer>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                assertEquals(meldekortdetaljer.id, responseObject.id)
            }
        }
    }

    @Test
    fun `get meldekortdetaljer returns Bad request with invalid fnr`() {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(id = "1",
            fodselsnr = "12345678910",
            kortType = KortType.AAP)

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                kontrollService = mockk(),
                mockInnsendtMeldekortService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")
            }.apply {
                val mapper = jacksonObjectMapper()
                assertNotNull(response.content)
                val responseObject = mapper.readValue<ErrorMessage>(response.content!!)
                response.status() shouldBe HttpStatusCode.BadRequest
                assertEquals("Personidentifikator matcher ikke. Bruker kan derfor ikke hente ut meldekortdetaljer.", responseObject.error)
            }
        }
    }

    @Test
    fun `get meldekortdetaljer returns 401-Unauthorized with missing JWT`() {
        val id: Long = 1
        val meldekortdetaljer = Meldekortdetaljer(id = "1",
            fodselsnr = "11111111111",
            kortType = KortType.AAP)

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                kontrollService = mockk(),
                mockInnsendtMeldekortService = mockk(),
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

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                kontrollService = mockk(),
                mockInnsendtMeldekortService = mockk(),
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
        val meldekortdetaljer = Meldekortdetaljer(id = "1",
            fodselsnr = "11111111111",
            kortType = KortType.AAP)

        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                kontrollService = mockk(),
                mockInnsendtMeldekortService = mockk(),
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
    fun `get korrigert meldekortid returns OK with valid JWT`() {
        val id: Long = 1
        val nyId: Long = 123

        coEvery { arenaOrdsService.kopierMeldekort(any()) } returns (nyId)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                kontrollService = mockk(),
                mockInnsendtMeldekortService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/meldekort/korrigering?meldekortId=${id}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")
            }.apply {
                val mapper = jacksonObjectMapper()
                assertNotNull(response.content)
                val responseObject = mapper.readValue<Long>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                assertEquals(nyId, responseObject)
            }
        }
    }
}