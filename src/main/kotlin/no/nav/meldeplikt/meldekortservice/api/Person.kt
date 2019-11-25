package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.config.extractIdentFromLoginContext
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortMapper
import no.nav.meldeplikt.meldekortservice.model.response.EmptyResponse
import no.nav.meldeplikt.meldekortservice.model.Meldeform
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.Meldeperiode
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.InnsendtMeldekortService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.PERSON_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrError
import no.nav.meldeplikt.meldekortservice.utils.swagger.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.Group
import kotlin.concurrent.thread

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente:
- Historiske meldekort
- Meldekort
- Endre meldeform
I tillegg til å sende inn/kontrollere meldekort.
 */
fun Routing.personApi(arenaOrdsService: ArenaOrdsService, innsendtMeldekortService: InnsendtMeldekortService) {
    getHistoriskeMeldekort(arenaOrdsService)
    getMeldekort(arenaOrdsService, innsendtMeldekortService)
    kontrollerMeldekort(innsendtMeldekortService)
    endreMeldeform(arenaOrdsService)
}

private val xmlMapper = XmlMapper()
private val jsonMapper = jacksonObjectMapper()

private const val personGroup = "Person"

@Group(personGroup)
@Location("$PERSON_PATH/historiskemeldekort")
data class HistoriskeMeldekortInput(val antallMeldeperioder: Int)

// Henter historiske meldekort
fun Routing.getHistoriskeMeldekort(arenaOrdsService: ArenaOrdsService) =
    get<HistoriskeMeldekortInput>(
        "Hent tidligerer/historiske meldekort".securityAndReponds(
            BearerTokenSecurity(),
            ok<Person>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
        historiskeMeldekortInput ->
        respondOrError {
            arenaOrdsService.hentHistoriskeMeldekort(
                extractIdentFromLoginContext(),
                historiskeMeldekortInput.antallMeldeperioder
            )
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/meldekort")
class MeldekortInput

// Henter meldekort
fun Routing.getMeldekort(arenaOrdsService: ArenaOrdsService, innsendtMeldekortService: InnsendtMeldekortService) =
    get<MeldekortInput>(
        "Hent meldekort".securityAndReponds(
            BearerTokenSecurity(),
            ok<Person>(),
            noContent<EmptyResponse>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
        respondOrError {
            val response = arenaOrdsService.hentMeldekort(extractIdentFromLoginContext())
            if (response.status == HttpStatusCode.OK) {
                MeldekortMapper.filtrerMeldekortliste(mapFraXml(response.content, Person::class.java), innsendtMeldekortService)
            } else {
                throw NoContentException()
            }
        }
    }

// Innsending/kontroll av meldekort (Amelding)
fun Routing.kontrollerMeldekort(innsendtMeldekortService: InnsendtMeldekortService) =
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

            if (kontrollertType.status == "OK") {
                innsendtMeldekortService.settInnInnsendtMeldekort(InnsendtMeldekort(kontrollertType.meldekortId))
            }
            call.respondText(jsonMapper.writeValueAsString(kontrollertType), contentType = ContentType.Application.Json)
        } catch (e: Exception) {
            val errorMessage = ErrorMessage("Meldekort med id ${meldekort.meldekortId} ble ikke sendt inn. ${e.message}")
            defaultLog.error(errorMessage.error, e)
            call.respond(status = HttpStatusCode.ServiceUnavailable, message = errorMessage)
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/meldeform")
class MeldeformInput

// Endre meldeform
fun Routing.endreMeldeform(arenaOrdsService: ArenaOrdsService) =
    post<MeldeformInput, Meldeform>(
        "Oppdater meldeform".securityAndReponds(
            BearerTokenSecurity(),
            ok<Meldeperiode>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {_, meldeform ->
        try {
            val meldeperiode = arenaOrdsService.endreMeldeform(extractIdentFromLoginContext(), meldeform.meldeformNavn)
            call.respond(meldeperiode)
        } catch (e: Exception) {
            val errorMessage = ErrorMessage("Kunne ikke endre meldeform til ${meldeform.meldeformNavn}. ${e.message}")
            defaultLog.error(errorMessage.error, e)
            call.respond(status = HttpStatusCode.ServiceUnavailable, message = errorMessage)
        }
    }

