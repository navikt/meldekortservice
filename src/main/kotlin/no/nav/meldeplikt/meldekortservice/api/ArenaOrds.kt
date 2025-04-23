package no.nav.meldeplikt.meldekortservice.api

import io.ktor.resources.Resource
import io.ktor.server.routing.Routing
import no.nav.meldeplikt.meldekortservice.model.ArenaOrdsSkrivemodus
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.SKRIVEMODUS_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrError
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjon for å sjekke om Arena ORDS er i skrivemodus.
 */
fun Routing.skrivemodusApi(arenaOrdsService: ArenaOrdsService) {
    getSkrivemodus(arenaOrdsService)
}

private const val arenaOrdsGroup = "Arena ORDS"

@Group(arenaOrdsGroup)
@Resource(SKRIVEMODUS_PATH)
class SkrivemodusInput

// Hent skrivemodus
fun Routing.getSkrivemodus(arenaOrdsService: ArenaOrdsService) =
    get<SkrivemodusInput>(
        "Hent Arena ORDS skrivemodus".securityAndResponse(
            BearerTokenSecurity(),
            ok<ArenaOrdsSkrivemodus>(),
            serviceUnavailable<ErrorMessage>(),
            badRequest<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {
        respondOrError {
            arenaOrdsService.hentSkrivemodus()
        }
    }
