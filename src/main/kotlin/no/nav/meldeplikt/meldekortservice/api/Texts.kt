package no.nav.meldeplikt.meldekortservice.api

import io.ktor.locations.*
import io.ktor.routing.*
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.TEXTS_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrError
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for text-api
 */
@KtorExperimentalLocationsAPI
fun Routing.textsApi(dbService: DBService) {
    exists(dbService)
    getOne(dbService)
    getAll(dbService)
}

private const val textsGroup = "Texts"

@Group(textsGroup)
@Location("$TEXTS_PATH/exists")
@KtorExperimentalLocationsAPI
data class ExistsInput(val id: String, val language: String, val from: String)

@KtorExperimentalLocationsAPI
fun Routing.exists(dbService: DBService) =
    get<ExistsInput>(
        "Sjekker at det finnes tekst med gitt nøkkel, språk og tid".securityAndResponse(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { input ->
        respondOrError {
            val result = dbService.getText(input.id, input.language, input.from)
            if (result != null) {
                input.id
            } else {
                input.id.split('-')[0]
            }
        }
    }

@Group(textsGroup)
@Location("$TEXTS_PATH/get")
@KtorExperimentalLocationsAPI
data class GetOneInput(val id: String, val language: String, val from: String)

@KtorExperimentalLocationsAPI
fun Routing.getOne(dbService: DBService) =
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
            dbService.getText(input.id, input.language, input.from) ?: input.id
        }
    }

@Group(textsGroup)
@Location("$TEXTS_PATH/getall")
@KtorExperimentalLocationsAPI
data class GetAllInput(val language: String, val from: String)

@KtorExperimentalLocationsAPI
fun Routing.getAll(dbService: DBService) =
    get<GetAllInput>(
        "Returnerer alle tekster med gitt språk og tid".securityAndResponse(
            BearerTokenSecurity(),
            ok<Any>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { input ->
        respondOrError {
            dbService.getTexts(input.language, input.from)
        }
    }

