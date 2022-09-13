package no.nav.meldeplikt.meldekortservice.api

import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.service.KontrollService
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

open class TestBase {

    companion object {
        const val ISSUER_ID = "default"
        const val REQUIRED_AUDIENCE = "default"

        var mockOAuth2Server = MockOAuth2Server()

        val env = Environment(dokarkivResendInterval = 0L)

        var dbService = mockk<DBService>()
        var arenaOrdsService = mockk<ArenaOrdsService>()
        var kontrollService = mockk<KontrollService>()
        var dokarkivService = mockk<DokarkivService>()

        @BeforeAll
        @JvmStatic
        fun setup() {
            mockOAuth2Server = MockOAuth2Server()
            mockOAuth2Server.start(8091)

            mockkStatic(::isCurrentlyRunningOnNais)
            every { isCurrentlyRunningOnNais() } returns true
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            mockOAuth2Server.shutdown()
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
            claims = mapOf("sub" to "01020312345")
        )
    ).serialize()

    fun issueTokenWithPid(): String = mockOAuth2Server.issueToken(
        ISSUER_ID,
        "myclient",
        DefaultOAuth2TokenCallback(
            audience = listOf(REQUIRED_AUDIENCE),
            claims = mapOf("pid" to "01020312345")
        )
    ).serialize()
}