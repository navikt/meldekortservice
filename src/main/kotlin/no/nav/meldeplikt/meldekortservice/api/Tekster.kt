package no.nav.meldeplikt.meldekortservice.api

import io.ktor.locations.*
import io.ktor.routing.*
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.TEKSTER_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrError
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for text-api
 */
@KtorExperimentalLocationsAPI
fun Routing.teksterApi(dbService: DBService) {
    eksisterer(dbService)
    hent(dbService)
    hentAlle(dbService)
}

private const val teksterGroup = "Tekster"

@Group(teksterGroup)
@Location("$TEKSTER_PATH/eksisterer")
@KtorExperimentalLocationsAPI
data class ExistsInput(val id: String, val language: String, val from: String)

@KtorExperimentalLocationsAPI
fun Routing.eksisterer(dbService: DBService) =
    get<ExistsInput>(
        "Sjekker at det finnes tekst med gitt nøkkel, språk og tid".securityAndResponse(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { input ->
        respondOrError {
            val result = dbService.hentTekst(input.id, input.language, input.from)
            if (result != null) {
                input.id
            } else {
                input.id.split('-')[0]
            }
        }
    }

@Group(teksterGroup)
@Location("$TEKSTER_PATH/hent")
@KtorExperimentalLocationsAPI
data class GetOneInput(val id: String, val language: String, val from: String)

@KtorExperimentalLocationsAPI
fun Routing.hent(dbService: DBService) =
    get<GetOneInput>(
        "Returnerer tekst med gitt nøkkel, språk og tid".securityAndResponse(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { input ->
        respondOrError {
            // If getText gives not null, return this result
            // If getText gives null, return input id
            dbService.hentTekst(input.id, input.language, input.from) ?: input.id
        }
    }

@Group(teksterGroup)
@Location("$TEKSTER_PATH/hentAlle")
@KtorExperimentalLocationsAPI
data class GetAllInput(val language: String, val from: String)

@KtorExperimentalLocationsAPI
fun Routing.hentAlle(dbService: DBService) =
    get<GetAllInput>(
        "Returnerer alle tekster med gitt språk og tid".securityAndResponse(
            BearerTokenSecurity(),
            ok<Any>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { input ->
        respondOrError {
            dbService.hentAlleTekster(input.language, input.from)
        }
    }

