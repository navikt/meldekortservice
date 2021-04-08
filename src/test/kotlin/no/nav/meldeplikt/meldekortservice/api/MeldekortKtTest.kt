package no.nav.meldeplikt.meldekortservice.api

import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.amshove.kluent.shouldBe
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

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
                    "sub" to "12345678910"
                )
            )
        ).serialize()

    companion object {
        private const val ISSUER_ID = "default"
        private const val REQUIRED_AUDIENCE = "default"

        val mockOAuth2Server = MockOAuth2Server()

        @BeforeAll
        @JvmStatic
        fun setup() {
            mockOAuth2Server.start(8091)
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            mockOAuth2Server.shutdown()
        }
    }

    @Test
    fun `Test hente meldekort`() {
        val id: Long = 1
        val idInt: Int = 10
        val meldekortdetaljer = Meldekortdetaljer(id = "1",
            kortType = KortType.AAP)

        val arenaOrdsService = mockk<ArenaOrdsService>()
        coEvery { arenaOrdsService.hentMeldekortdetaljer(any()) } returns (meldekortdetaljer)

        val flywayConfig = mockk<Flyway>()
        every { flywayConfig.migrate() } returns 0

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
           mainModule(arenaOrdsService  = arenaOrdsService,
            kontrollService = mockk(),
            innsendtMeldekortService = mockk(),
            flywayConfig = flywayConfig,
            env = Environment(
            oauthClientId = "test",
            oauthJwk = "test",
            oauthClientSecret = "test",
            oauthEndpoint = "test",
            oauthTenant = "test",
            dbHostPostgreSQL = "jdbc:h2:mem:testdb",
            dbUrlPostgreSQL = "jdbc:h2:mem:testdb",
            dbUserPostgreSQL = "sa",
            dbPasswordPostgreSQL = ""

            )
           )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/historiskemeldekort/")
            {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")

            }
               .apply {
                    response.status() shouldBe HttpStatusCode.OK
                }
        }


    }

}