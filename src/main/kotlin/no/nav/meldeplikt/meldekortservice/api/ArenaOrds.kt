package no.nav.meldeplikt.meldekortservice.api

import io.ktor.server.locations.*
import io.ktor.server.routing.*
import no.nav.meldeplikt.meldekortservice.model.ArenaOrdsSkrivemodus
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjon for å sjekke om Arena ORDS er i skrivemodus.
 */
@KtorExperimentalLocationsAPI
fun Routing.skrivemodusApi(arenaOrdsService: ArenaOrdsService) {
    getSkrivemodus(arenaOrdsService)
}

private const val skrivemodusGroup = "Skrivemodus"

@Group(skrivemodusGroup)
@Location(SKRIVEMODUS_PATH)
@KtorExperimentalLocationsAPI
class SkrivemodusInput

// Hent skrivemodus
@KtorExperimentalLocationsAPI
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
            val response = arenaOrdsService.hentSkrivemodus()
            defaultObjectMapper.readValue(response.content, ArenaOrdsSkrivemodus::class.java)
        }
    }
