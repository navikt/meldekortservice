package no.nav.meldeplikt.meldekortservice.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.meldeplikt.meldekortservice.config.userIdent
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortMapper
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortkontrollMapper
import no.nav.meldeplikt.meldekortservice.model.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.database.feil.UnretriableDatabaseException
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.response.EmptyResponse
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.service.KontrollService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente:
- Historiske meldekort
- Meldekort
I tillegg til å sende inn/kontrollere meldekort.
 */
@KtorExperimentalLocationsAPI
fun Routing.personApi(
    arenaOrdsService: ArenaOrdsService,
    dbService: DBService,
    kontrollService: KontrollService,
    dokarkivService: DokarkivService
) {
    getHistoriskeMeldekort(arenaOrdsService)
    getMeldekort(arenaOrdsService, dbService)
    kontrollerMeldekort(kontrollService, dbService)
    opprettJournalpost(dokarkivService, dbService)
}

private val meldekortkontrollMapper = MeldekortkontrollMapper()

private const val personGroup = "Person"

@Group(personGroup)
@Location("$PERSON_PATH/historiskemeldekort")
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
@Location("$PERSON_PATH/meldekort")
@KtorExperimentalLocationsAPI
class MeldekortInput

// Henter meldekort
@KtorExperimentalLocationsAPI
fun Routing.getMeldekort(arenaOrdsService: ArenaOrdsService, dbService: DBService) =
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
                MeldekortMapper.filtrerMeldekortliste(
                    mapFraXml(response.content, Person::class.java),
                    dbService
                )
            } else {
                throw NoContentException()
            }
        }
    }

// Innsending/kontroll av meldekort
@KtorExperimentalLocationsAPI
fun Routing.kontrollerMeldekort(kontrollService: KontrollService, dbService: DBService) =
    post(
        "Kontroll/innsending av meldekort".securityAndResponse(
            BearerTokenSecurity(),
            ok<MeldekortKontrollertType>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { _: MeldekortInput, meldekort: Meldekortdetaljer ->
        try {
            val kontrollResponse = kontrollService.kontroller(
                meldekort = meldekortkontrollMapper.mapMeldekortTilMeldekortkontroll(meldekort)
            )
            if (kontrollResponse.arsakskoder.arsakskode.size == 0) {
                try {
                    dbService.settInnInnsendtMeldekort(InnsendtMeldekort(kontrollResponse.meldekortId))
                } catch (e: UnretriableDatabaseException) {
                    // Meldekort er sendt inn ok til baksystem, men det oppstår feil ved skriving til MIP-tabellen i databasen.
                    // Logger warning, og returnerer ok status til brukeren, slik at bruker ikke forsøker å sende inn meldekortet på
                    // nytt (gir dubletter).
                    val errorMessage =
                        ErrorMessage("Meldekort med id ${meldekort.meldekortId} ble sendt inn, men applikasjonen klarte ikke å skrive til MIP-tabellen. ${e.message}")
                    defaultLog.warn(errorMessage.error, e)
                }
            }

            // Send kontroll-responsen tilbake som respons
            call.respondText(
                defaultObjectMapper.writeValueAsString(kontrollResponse),
                contentType = ContentType.Application.Json
            )
        } catch (e: Exception) {
            val errorMessage =
                ErrorMessage("Meldekort med id ${meldekort.meldekortId} ble ikke sendt inn. ${e.message}")
            defaultLog.warn(errorMessage.error, e)
            defaultLog.info("Exception fra meldekortkontroll for meldekort: "
                    + defaultObjectMapper.writeValueAsString(meldekort))
            call.respond(status = HttpStatusCode.ServiceUnavailable, message = errorMessage)
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/opprettJournalpost")
@KtorExperimentalLocationsAPI
class JournalpostInput

// Opprett journalpost i dokarkiv
@KtorExperimentalLocationsAPI
fun Routing.opprettJournalpost(
    dokarkivService: DokarkivService,
    dbService: DBService
) =
    post(
        "Opprett journalpost i dokarkiv".securityAndResponse(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { _: JournalpostInput, journalpost: Journalpost ->
        val meldekortId = journalpost.tilleggsopplysninger!!.first { it.nokkel == "meldekortId" }.verdi

        try {
            val journalpostResponse = dokarkivService.createJournalpost(journalpost)
            defaultLog.info("Journalpost med id " + journalpostResponse.journalpostId + " opprettet for meldekort med id $meldekortId")

            dbService.lagreJournalpostData(
                journalpostResponse.journalpostId,
                journalpostResponse.dokumenter[0].dokumentInfoId,
                meldekortId.toLong()
            )

            call.respond(status = HttpStatusCode.OK, message = "Journalpost opprettet")
        } catch (e: Exception) {
            val errorMessage = ErrorMessage(
                "Kan ikke opprette journalpost i dokumentarkiv med eksternReferanseId ${journalpost.eksternReferanseId} for meldekort med id $meldekortId"
            )
            defaultLog.warn(errorMessage.error, e)

            dbService.lagreJournalpostMidlertidig(journalpost)

            // Vi sender OK tilbake for å gi mulighet å gå videre go vise kvittering
            // Meldekort har jo blitt sendt, mens journalpost kan opprettes senere
            call.respond(status = HttpStatusCode.OK, message = errorMessage)
        }
    }
