package no.nav.meldeplikt.meldekortservice.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.meldeplikt.meldekortservice.config.userIdent
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.response.EmptyResponse
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente:
- Historiske meldekort
- Meldekort
 */
@KtorExperimentalLocationsAPI
fun Routing.personApi(
    arenaOrdsService: ArenaOrdsService,
    dbService: DBService,
    dokarkivService: DokarkivService
) {
    getHistoriskeMeldekort(arenaOrdsService)
    getMeldekort(arenaOrdsService)
    opprettJournalpost(dokarkivService, dbService)
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

@Group(personGroup)
@Location(OPPRETT_JOURNALPOST_PATH)
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

            call.respondText("Journalpost opprettet")
        } catch (e: Exception) {
            val errorMessage = ErrorMessage(
                "Kan ikke opprette journalpost i dokumentarkiv med eksternReferanseId ${journalpost.eksternReferanseId} for meldekort med id $meldekortId"
            )
            defaultLog.warn(errorMessage.error, e)

            dbService.lagreJournalpostMidlertidig(journalpost)

            // Vi sender OK tilbake for å gi mulighet å gå videre og vise kvittering
            // Meldekort har jo blitt sendt, mens journalpost kan opprettes senere
            call.respond(status = HttpStatusCode.OK, message = errorMessage)
        }
    }
