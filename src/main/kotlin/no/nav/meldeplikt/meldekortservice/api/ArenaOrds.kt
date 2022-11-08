package no.nav.meldeplikt.meldekortservice.api

import io.ktor.server.locations.*
import io.ktor.server.routing.*
import no.nav.meldeplikt.meldekortservice.config.userIdent
import no.nav.meldeplikt.meldekortservice.model.ArenaOrdsLesemodus
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjon for å sjekke om Arena ORDS er i lesemodus.
 */
@KtorExperimentalLocationsAPI
fun Routing.lesemodusApi(arenaOrdsService: ArenaOrdsService) {
    getLesemodus(arenaOrdsService)
}

private const val lesemodusGroup = "Lesemodus"

@Group(lesemodusGroup)
@Location(LESEMODUS_PATH)
@KtorExperimentalLocationsAPI
class LesemodusInput

// Hent lesemodus
@KtorExperimentalLocationsAPI
fun Routing.getLesemodus(arenaOrdsService: ArenaOrdsService) =
    get<LesemodusInput>(
        "Hent Arena ORDS lesemodus".securityAndResponse(
            BearerTokenSecurity(),
            ok<ArenaOrdsLesemodus>(),
            serviceUnavailable<ErrorMessage>(),
            badRequest<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { respondOrError {
            arenaOrdsService.hentLesemodus()
        }
    }
