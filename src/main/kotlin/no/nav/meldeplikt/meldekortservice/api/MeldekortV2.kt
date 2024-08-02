package no.nav.meldeplikt.meldekortservice.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.routing.*
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.model.meldegruppe.Meldegruppe
import no.nav.meldeplikt.meldekortservice.model.meldegruppe.MeldegruppeResponse
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.*
import java.time.LocalDate

/**
 * REST-controller som henter FNR fra ident-header og tilbyr operasjoner for å hente:
 * - Meldekort
 * - Historiske meldekort
 * - Meldekortdetaljer
 * - Korrigert meldekort (ber Arena å opprette et nytt meldekort basert på meldekort med angitt ID og returnerer ny ID)
 */

class Headers(val ident: String)

@KtorExperimentalLocationsAPI
fun Routing.meldekortApiV2(arenaOrdsService: ArenaOrdsService) {
    hentMeldekort(arenaOrdsService)
    hentHistoriskeMeldekort(arenaOrdsService)
    hentMeldekortdetaljer(arenaOrdsService)
    hentKorrigertMeldekort(arenaOrdsService)
    hentMeldegrupper(arenaOrdsService)
}

private const val group = "Meldekort v2"

@Group(group)
@Location("$API_PATH/v2/meldekort")
@KtorExperimentalLocationsAPI
class HentMeldekortInput

@KtorExperimentalLocationsAPI
fun Routing.hentMeldekort(arenaOrdsService: ArenaOrdsService) = get<HentMeldekortInput>(
    "Hent meldekort".securityAndResponse(
        BearerTokenSecurity(),
        ok<Person>(),
        badRequest<ErrorMessage>(),
        serviceUnavailable<ErrorMessage>(),
        unAuthorized<Error>()
    ).header<Headers>()
) {
    respondOrError {
        val ident = getIdent(call)

        val response = arenaOrdsService.hentMeldekort(ident)
        if (response.status == HttpStatusCode.OK) {
            mapFraXml(response.content, Person::class.java)
        } else {
            throw NoContentException()
        }
    }
}

@Group(group)
@Location("$API_PATH/v2/historiskemeldekort")
@KtorExperimentalLocationsAPI
class HentHistoriskeMeldekortInput(val antallMeldeperioder: Int)

@KtorExperimentalLocationsAPI
fun Routing.hentHistoriskeMeldekort(arenaOrdsService: ArenaOrdsService) = get<HentHistoriskeMeldekortInput>(
    "Hent tidligere/historiske meldekort".securityAndResponse(
        BearerTokenSecurity(),
        ok<Person>(),
        badRequest<ErrorMessage>(),
        serviceUnavailable<ErrorMessage>(),
        unAuthorized<Error>()
    ).header<Headers>()
) { hentHistoriskeMeldekortInput ->
    respondOrError {
        val ident = getIdent(call)

        arenaOrdsService.hentHistoriskeMeldekort(
            ident,
            hentHistoriskeMeldekortInput.antallMeldeperioder
        )
    }
}

@Group(group)
@Location("$API_PATH/v2/meldekortdetaljer")
@KtorExperimentalLocationsAPI
data class HentMeldekortdetaljerInput(val meldekortId: Long)

@KtorExperimentalLocationsAPI
fun Routing.hentMeldekortdetaljer(arenaOrdsService: ArenaOrdsService) =
    get<HentMeldekortdetaljerInput>(
        "Hent meldekortdetaljer".securityAndResponse(
            BearerTokenSecurity(),
            ok<Meldekortdetaljer>(),
            badRequest<ErrorMessage>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        ).header<Headers>()
    ) { hentMeldekortdetaljerInput ->
        respondOrError {
            val ident = getIdent(call)

            val meldekortdetaljer = arenaOrdsService.hentMeldekortdetaljer(hentMeldekortdetaljerInput.meldekortId)
            if (meldekortdetaljer.meldegruppe == "") meldekortdetaljer.meldegruppe = "NULL"
            if (meldekortdetaljer.fodselsnr == ident) {
                meldekortdetaljer
            } else {
                val msg = "Personidentifikator matcher ikke. Bruker kan derfor ikke hente ut meldekortdetaljer."
                defaultLog.warn(msg)
                throw SecurityException(msg)
            }
        }
    }

@Group(group)
@Location("$API_PATH/v2/korrigertMeldekort")
@KtorExperimentalLocationsAPI
data class HentKorrigertMeldekortInput(
    val meldekortId: Long
)

@KtorExperimentalLocationsAPI
fun Routing.hentKorrigertMeldekort(arenaOrdsService: ArenaOrdsService) =
    get<HentKorrigertMeldekortInput>(
        "Hent korrigert meldekortid".securityAndResponse(
            BearerTokenSecurity(),
            ok<String>(),
            badRequest<ErrorMessage>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        ).header<Headers>()
    ) { hentKorrigertMeldekortInput ->
        respondOrError {
            val ident = getIdent(call)

            val meldekortdetaljer = arenaOrdsService.hentMeldekortdetaljer(hentKorrigertMeldekortInput.meldekortId)
            if (meldekortdetaljer.fodselsnr != ident) {
                val msg = "Personidentifikator matcher ikke. Bruker kan derfor ikke korrigere meldekort."
                defaultLog.warn(msg)
                throw SecurityException()
            }

            arenaOrdsService.kopierMeldekort(hentKorrigertMeldekortInput.meldekortId)
        }
    }

@Group(group)
@Location("$API_PATH/v2/meldegrupper")
@KtorExperimentalLocationsAPI
class HentMeldegrupperInput

@KtorExperimentalLocationsAPI
fun Routing.hentMeldegrupper(arenaOrdsService: ArenaOrdsService) = get<HentMeldegrupperInput>(
    "Hent meldegrupper".securityAndResponse(
        BearerTokenSecurity(),
        ok<List<Meldegruppe>>(),
        badRequest<ErrorMessage>(),
        serviceUnavailable<ErrorMessage>(),
        unAuthorized<Error>()
    ).header<Headers>()
) {
    respondOrError {
        val ident = getIdent(call)

        val result = arenaOrdsService.hentMeldegrupper(ident, LocalDate.now())

        result.meldegruppeListe
    }
}

private fun getIdent(call: ApplicationCall): String {
    val ident = call.request.headers["ident"]
    if (ident.isNullOrBlank() || ident.length != 11) {
        throw SecurityException("Personidentifikator finnes ikke")
    }

    return ident
}
