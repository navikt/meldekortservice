package no.nav.meldeplikt.meldekortservice.api

import io.ktor.locations.*
import io.ktor.routing.*
import no.nav.meldeplikt.meldekortservice.config.userIdent
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente meldekortdetaljer og korrigering for en NAV-bruker.
 */
fun Routing.meldekortApi(arenaOrdsService: ArenaOrdsService) {
    getMeldekortdetaljer(arenaOrdsService)
    getKorrigertMeldekort(arenaOrdsService)
}

private const val meldekortGroup = "Meldekort"

@Group(meldekortGroup)
@Location("$MELDEKORT_PATH")
@KtorExperimentalLocationsAPI
data class MeldekortdetaljerInput(val meldekortId: Long)

// Hent meldekortdetaljer
@KtorExperimentalLocationsAPI
fun Routing.getMeldekortdetaljer(arenaOrdsService: ArenaOrdsService) =
    get<MeldekortdetaljerInput>(
        "Hent meldekortdetaljer".securityAndResponse(
            BearerTokenSecurity(),
            ok<Meldekortdetaljer>(),
            serviceUnavailable<ErrorMessage>(),
            badRequest<ErrorMessage>(),
            unAuthorized<Error>())) {
            meldekortdetaljerInput -> respondOrError {

            var meldekortdetaljer = arenaOrdsService.hentMeldekortdetaljer(meldekortdetaljerInput.meldekortId)
            if (meldekortdetaljer.meldegruppe == null) meldekortdetaljer.meldegruppe =  "NULL"
            if (meldekortdetaljer.fodselsnr == userIdent) {
                meldekortdetaljer
            } else {
                val msg = "Personidentifikator matcher ikke. Bruker kan derfor ikke hente ut meldekortdetaljer."
                defaultLog.warn(msg)
                throw SecurityException(msg)
            }
        }
    }

@Group(meldekortGroup)
@Location("$MELDEKORT_PATH/korrigering")
@KtorExperimentalLocationsAPI
data class KorrigertMeldekortInput(val meldekortId: Long)

// Henter meldekortid for nytt (korrigert) kort
@KtorExperimentalLocationsAPI
fun Routing.getKorrigertMeldekort(arenaOrdsService: ArenaOrdsService) =
    get<KorrigertMeldekortInput>(
        "Hent korrigert meldekortid".securityAndResponse(
            BearerTokenSecurity(),
            ok<String>(),
            serviceUnavailable<ErrorMessage>(),
            badRequest<ErrorMessage>(),
            unAuthorized<Error>())) {
            korrigertMeldekortInput -> respondOrError{

            arenaOrdsService.kopierMeldekort(korrigertMeldekortInput.meldekortId)
        }
    }