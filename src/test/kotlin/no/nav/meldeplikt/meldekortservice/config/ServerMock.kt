package no.nav.meldeplikt.meldekortservice.config

import io.ktor.application.Application
import io.mockk.mockk
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.InnsendtMeldekortService
import no.nav.meldeplikt.meldekortservice.service.KontrollService
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.*

internal fun ServerMock(
        arenaOrdsService: ArenaOrdsService  = mockk(),
        kontrollService: KontrollService = mockk(),
        innsendtMeldekortService: InnsendtMeldekortService = mockk(),
        flywayConfig: Flyway = mockk<Flyway>(),
        env: Environment = Environment(
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
): Application.() -> Unit {
    return fun Application.() {
        mainModule(
                true,
                env,
                innsendtMeldekortService,
                arenaOrdsService,
                kontrollService,
                flywayConfig
        )
    }
}
