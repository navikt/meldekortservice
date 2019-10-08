package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.Amelding
import no.nav.meldeplikt.meldekortservice.model.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.swagger.*
import no.nav.meldeplikt.meldekortservice.utils.PERSON_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrServiceUnavailable
import no.nav.meldeplikt.meldekortservice.utils.swagger.Group


fun Routing.personApi(httpClient: HttpClient) {
    getHistoriskeMeldekort()
    getStatus()
    getMeldekort()
    kontrollerMeldekort()
}

private const val personGroup = "Person"

@Group(personGroup)
@Location("$PERSON_PATH/historiskemeldekort")
class GetHistoriskeMeldekort

fun Routing.getHistoriskeMeldekort() =
    get<GetHistoriskeMeldekort>(
        "Hent tidligerer/historiske meldekort".securityAndReponds(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
        respondOrServiceUnavailable {
            "Historiske meldekort er ikke implementert"
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/status")
class GetStatus

fun Routing.getStatus() =
    get<GetStatus>(
        "Hent personstatus".securityAndReponds(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
        respondOrServiceUnavailable {
            "Status er ikke implementert"
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/meldekort")
class GetMeldekort

fun Routing.getMeldekort() =
    get<GetMeldekort>(
        "Hent meldekort".securityAndReponds(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
        respondOrServiceUnavailable {
            "Meldekort er ikke implementert"
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/meldekort")
class KontrollerMeldekort

fun Routing.kontrollerMeldekort() =
    post<KontrollerMeldekort, Meldekortdetaljer>(
        "Kontrollering/innsending av meldekort til Amelding".securityAndReponds(
            BearerTokenSecurity(),
            ok<MeldekortKontrollertType>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {_, meldekort ->

        try {
            val kontrollertType = Amelding.ameldingService().kontrollerMeldekort(meldekort)
            call.respond(kontrollertType)
        } catch (e: Exception) {
            val errorMessage = ErrorMessage("Meldekort ble ikke sendt inn. ${e.message}")
            call.respond(status = HttpStatusCode.ServiceUnavailable, message = errorMessage)
        }
    }
