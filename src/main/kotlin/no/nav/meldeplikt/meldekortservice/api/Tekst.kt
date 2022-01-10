package no.nav.meldeplikt.meldekortservice.api

import io.ktor.locations.*
import io.ktor.routing.*
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.TEKST_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrError
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for tekst-api som tilbyr operasjoner for å:
- sjekke at tekst med gitt kode, språr og tidspunkt eksisterer
- hente tekst med gitt kode, språr og tidspunkt
- hente alle tekster med gitt språk og tidspunkt
 */
@KtorExperimentalLocationsAPI
fun Routing.tekstApi(dbService: DBService) {
    eksisterer(dbService)
    hent(dbService)
    hentAlle(dbService)
}

private const val teksterGroup = "Tekster"

@Group(teksterGroup)
@Location("$TEKST_PATH/eksisterer")
@KtorExperimentalLocationsAPI
data class ExistsInput(val kode: String, val sprak: String, val fraTidspunkt: String)

@KtorExperimentalLocationsAPI
fun Routing.eksisterer(dbService: DBService) =
    get<ExistsInput>(
        "Sjekker at det finnes tekst med gitt kode, språk og tidspunkt".securityAndResponse(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { input ->
        respondOrError {
            val result = dbService.hentTekst(input.kode, input.sprak, input.fraTidspunkt)
            if (result != null) {
                input.kode
            } else {
                input.kode.split('-')[0]
            }
        }
    }

@Group(teksterGroup)
@Location("$TEKST_PATH/hent")
@KtorExperimentalLocationsAPI
data class GetOneInput(val kode: String, val sprak: String, val fraTidspunkt: String)

@KtorExperimentalLocationsAPI
fun Routing.hent(dbService: DBService) =
    get<GetOneInput>(
        "Returnerer tekst med gitt kode, språk og tidspunkt".securityAndResponse(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { input ->
        respondOrError {
            // If hentTekst gives not null, return this result
            // If hentTekst gives null, return input id
            dbService.hentTekst(input.kode, input.sprak, input.fraTidspunkt) ?: input.kode
        }
    }

@Group(teksterGroup)
@Location("$TEKST_PATH/hentAlle")
@KtorExperimentalLocationsAPI
data class GetAllInput(val sprak: String, val fraTidspunkt: String)

@KtorExperimentalLocationsAPI
fun Routing.hentAlle(dbService: DBService) =
    get<GetAllInput>(
        "Returnerer alle tekster med gitt språk og tidspunkt".securityAndResponse(
            BearerTokenSecurity(),
            ok<Any>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { input ->
        respondOrError {
            dbService.hentAlleTekster(input.sprak, input.fraTidspunkt)
        }
    }

