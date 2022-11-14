package no.nav.meldeplikt.meldekortservice.api

import io.ktor.server.config.*
import io.ktor.server.locations.*
import io.ktor.server.testing.*
import io.mockk.*
import no.nav.meldeplikt.meldekortservice.config.DUMMY_FNR
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

@KtorExperimentalLocationsAPI
open class TestBase {

    companion object {
        const val ISSUER_ID = "default"
        const val REQUIRED_AUDIENCE = "default"

        var mockOAuth2Server = MockOAuth2Server()

        val env = Environment(dokarkivResendInterval = 0L)

        var dbService = mockk<DBService>()
        var arenaOrdsService = mockk<ArenaOrdsService>()
        var dokarkivService = mockk<DokarkivService>()

        @BeforeAll
        @JvmStatic
        fun setup() {
            mockOAuth2Server = MockOAuth2Server()
            mockOAuth2Server.start(8091)

            every { dbService.lagreKallLogg(any()) } returns 1L
            every { dbService.lagreResponse(any(), any(), any()) } just Runs

            mockkStatic(::isCurrentlyRunningOnNais)
            every { isCurrentlyRunningOnNais() } returns true
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            mockOAuth2Server.shutdown()
        }
    }

    fun setUpTestApplication(block: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
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
                    mockDokarkivService = dokarkivService
                )
            }

            block()
        }
    }

    fun setOidcConfig(): MapApplicationConfig {
        return MapApplicationConfig(
            "ktor.environment" to "test",
            "no.nav.security.jwt.issuers.size" to "1",
            "no.nav.security.jwt.issuers.0.issuer_name" to ISSUER_ID,
            "no.nav.security.jwt.issuers.0.discoveryurl" to mockOAuth2Server.wellKnownUrl(ISSUER_ID).toString(),
            "no.nav.security.jwt.issuers.0.accepted_audience" to REQUIRED_AUDIENCE,
            "ktor.environment" to "local"
        )
    }

    fun issueTokenWithSub(): String = mockOAuth2Server.issueToken(
        ISSUER_ID,
        "myclient",
        DefaultOAuth2TokenCallback(
            audience = listOf(REQUIRED_AUDIENCE),
            claims = mapOf("sub" to DUMMY_FNR)
        )
    ).serialize()

    fun issueTokenWithPid(): String = mockOAuth2Server.issueToken(
        ISSUER_ID,
        "myclient",
        DefaultOAuth2TokenCallback(
            audience = listOf(REQUIRED_AUDIENCE),
            claims = mapOf("pid" to DUMMY_FNR)
        )
    ).serialize()
}
