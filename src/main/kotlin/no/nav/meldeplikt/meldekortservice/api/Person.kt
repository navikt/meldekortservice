package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.config.extractIdentFromLoginContext
import no.nav.meldeplikt.meldekortservice.model.Meldeform
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.Meldeperiode
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.swagger.*
import no.nav.meldeplikt.meldekortservice.utils.PERSON_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrServiceUnavailable
import no.nav.meldeplikt.meldekortservice.utils.swagger.Group

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente:
- Historiske meldekort
- Personstatus
- Meldekort
I tillegg sende inn/kontrollere meldekort
 */
fun Routing.personApi(httpClient: HttpClient) {
    getHistoriskeMeldekort()
    getStatus()
    getMeldekort()
    kontrollerMeldekort()
    endreMeldeform()
}

private const val personGroup = "Person"

@Group(personGroup)
@Location("$PERSON_PATH/historiskemeldekort")
data class HistoriskeMeldekortInput(val antallMeldeperioder: Int)

// Henter historiske meldekort
fun Routing.getHistoriskeMeldekort() =
    get<HistoriskeMeldekortInput>(
        "Hent tidligerer/historiske meldekort".securityAndReponds(
            BearerTokenSecurity(),
            ok<Person>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
        historiskeMeldekortInput -> respondOrServiceUnavailable {
            ArenaOrdsService.hentHistoriskeMeldekort(
                extractIdentFromLoginContext(),
                historiskeMeldekortInput.antallMeldeperioder
            )
        }
    }

// Henter personstatus (arenastatus)
@Group(personGroup)
@Location("$PERSON_PATH/status")
class StatusInput

fun Routing.getStatus() =
    get<StatusInput>(
        "Hent personstatus".securityAndReponds(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
        respondOrServiceUnavailable {
            "Status er ikke implementert.}"
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/meldekort")
class MeldekortInput

// Henter meldekort
fun Routing.getMeldekort() =
    get<MeldekortInput>(
        "Hent meldekort".securityAndReponds(
            BearerTokenSecurity(),
            ok<Person>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
        respondOrServiceUnavailable {
            ArenaOrdsService.hentMeldekort(extractIdentFromLoginContext())
        }
    }

// Innsending/kontroll av meldekort (Amelding)
fun Routing.kontrollerMeldekort() =
    post<MeldekortInput, Meldekortdetaljer>(
        "Kontroll/innsending av meldekort til Amelding".securityAndReponds(
            BearerTokenSecurity(),
            ok<MeldekortKontrollertType>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {_, meldekort ->

        try {
            val kontrollertType = SoapConfig.soapService().kontrollerMeldekort(meldekort)
            call.respond(kontrollertType)
        } catch (e: Exception) {
            val errorMessage = ErrorMessage("Meldekort ble ikke sendt inn. ${e.message}")
            call.respond(status = HttpStatusCode.ServiceUnavailable, message = errorMessage)
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/meldeform")
class MeldeformInput

// Endre meldeform
fun Routing.endreMeldeform() =
    post<MeldeformInput, Meldeform>(
        "Oppdater meldeform".securityAndReponds(
            BearerTokenSecurity(),
            ok<Meldeperiode>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {_, meldeform ->
        call.respond(status=HttpStatusCode.OK,message = "Meldeform er ikke implementert")
    }