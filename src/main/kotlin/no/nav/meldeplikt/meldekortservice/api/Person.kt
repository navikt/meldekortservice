package no.nav.meldeplikt.meldekortservice.api

import io.ktor.http.*
import io.ktor.server.locations.*
import io.ktor.server.routing.*
import no.nav.meldeplikt.meldekortservice.config.userIdent
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.response.EmptyResponse
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente:
- Historiske meldekort
- Meldekort
 */
@KtorExperimentalLocationsAPI
fun Routing.personApi(arenaOrdsService: ArenaOrdsService) {
    getHistoriskeMeldekort(arenaOrdsService)
    getMeldekort(arenaOrdsService)
}

private const val personGroup = "Person"

@Group(personGroup)
@Location(HISTORISKE_MELDEKORT_PATH)
@KtorExperimentalLocationsAPI
data class HistoriskeMeldekortInput(val antallMeldeperioder: Int)

// Henter historiske meldekort
@KtorExperimentalLocationsAPI
fun Routing.getHistoriskeMeldekort(arenaOrdsService: ArenaOrdsService) =
    get<HistoriskeMeldekortInput>(
        "Hent tidligere/historiske meldekort".securityAndResponse(
            BearerTokenSecurity(),
            ok<Person>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { historiskeMeldekortInput ->
        respondOrError {
            arenaOrdsService.hentHistoriskeMeldekort(
                userIdent,
                historiskeMeldekortInput.antallMeldeperioder
            )
        }
    }

@Group(personGroup)
@Location(PERSON_MELDEKORT_PATH)
@KtorExperimentalLocationsAPI
class MeldekortInput

// Henter meldekort
@KtorExperimentalLocationsAPI
fun Routing.getMeldekort(arenaOrdsService: ArenaOrdsService) =
    get<MeldekortInput>(
        "Hent meldekort".securityAndResponse(
            BearerTokenSecurity(),
            ok<Person>(),
            noContent<EmptyResponse>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {
        respondOrError {
            val response = arenaOrdsService.hentMeldekort(userIdent)
            if (response.status == HttpStatusCode.OK) {
                mapFraXml(response.content, Person::class.java)
            } else {
                throw NoContentException()
            }
        }
    }
