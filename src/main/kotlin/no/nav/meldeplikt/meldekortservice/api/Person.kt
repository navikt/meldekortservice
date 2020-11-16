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
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.config.userIdent
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortMapper
import no.nav.meldeplikt.meldekortservice.model.response.EmptyResponse
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.database.feil.UnretriableDatabaseException
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

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente:
- Historiske meldekort
- Meldekort
I tillegg til å sende inn/kontrollere meldekort.
 */
fun Routing.personApi(arenaOrdsService: ArenaOrdsService, innsendtMeldekortService: InnsendtMeldekortService) {
    getHistoriskeMeldekort(arenaOrdsService)
    getMeldekort(arenaOrdsService, innsendtMeldekortService)
    kontrollerMeldekort(innsendtMeldekortService)
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
            unAuthorized<Error>()
        )
    ) {
        respondOrError {
            val response = arenaOrdsService.hentMeldekort(userIdent)
            if (response.status == HttpStatusCode.OK) {
                MeldekortMapper.filtrerMeldekortliste(
                    mapFraXml(response.content, Person::class.java),
                    innsendtMeldekortService
                )
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
    ) { _, meldekort ->
        try {
            val kontrollertType = SoapConfig.soapService().kontrollerMeldekort(meldekort)

            if (kontrollertType.status == "OK") {
                try {
                    innsendtMeldekortService.settInnInnsendtMeldekort(InnsendtMeldekort(kontrollertType.meldekortId))
                } catch (e: UnretriableDatabaseException) {
                    // Meldekort er sendt inn ok til baksystem, men det oppstår feil ved skriving til MIP-tabellen i databasen.
                    // Logger warning, og returnerer ok status til brukeren slik at den ikke forsøker å sende inn meldekortet på
                    // nytt (gir dubletter).
                    val errorMessage =
                        ErrorMessage("Meldekort med id ${meldekort.meldekortId} ble sendt inn, men klarte ikke å skrive til MIP-tabellen. ${e.message}")
                    defaultLog.warn(errorMessage.error, e)
                }
            }
            call.respondText(jsonMapper.writeValueAsString(kontrollertType), contentType = ContentType.Application.Json)
        } catch (e: Exception) {
            val errorMessage =
                ErrorMessage("Meldekort med id ${meldekort.meldekortId} ble ikke sendt inn. ${e.message}")
            defaultLog.error(errorMessage.error, e)
            call.respond(status = HttpStatusCode.ServiceUnavailable, message = errorMessage)
        }
    }
