package no.nav.meldeplikt.meldekortservice.api

import io.ktor.client.HttpClient
import io.ktor.locations.Location
import io.ktor.routing.Routing
import no.nav.meldeplikt.meldekortservice.config.extractIdentFromLoginContext
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.MELDEKORT_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrServiceUnavailable
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente meldekortdetaljer og korrigering for en NAV-bruker.
 */
fun Routing.meldekortApi() {
    getMeldekortdetaljer()
    getKorrigertMeldekort()
}

private const val meldekortGroup = "Meldekort"

@Group(meldekortGroup)
@Location("$MELDEKORT_PATH")
data class MeldekortdetaljerInput(val meldekortId: Long)

// Hent meldekortdetaljer
fun Routing.getMeldekortdetaljer() =
    get<MeldekortdetaljerInput>(
        "Hent meldekortdetaljer".securityAndReponds(
            BearerTokenSecurity(),
            ok<Meldekortdetaljer>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>())) {
            meldekortdetaljerInput -> respondOrServiceUnavailable {

            val meldekortdetaljer = ArenaOrdsService.hentMeldekortdetaljer(meldekortdetaljerInput.meldekortId)
            if (meldekortdetaljer.fodselsnr == extractIdentFromLoginContext()) {
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
data class KorrigertMeldekortInput(val meldekortId: Long)

// Henter meldekortid for nytt (korrigert) kort
fun Routing.getKorrigertMeldekort() =
    get<KorrigertMeldekortInput>(
        "Hent korrigert meldekortid".securityAndReponds(BearerTokenSecurity(), ok<String>(),
            serviceUnavailable<ErrorMessage>(), unAuthorized<Error>())) {
            korrigertMeldekortInput -> respondOrServiceUnavailable{

            ArenaOrdsService.kopierMeldekort(korrigertMeldekortInput.meldekortId)
        }
    }