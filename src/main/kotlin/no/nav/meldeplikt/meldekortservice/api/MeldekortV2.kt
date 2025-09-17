package no.nav.meldeplikt.meldekortservice.api

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Routing
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.model.meldegruppe.Meldegruppe
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldestatus.MeldestatusRequest
import no.nav.meldeplikt.meldekortservice.model.meldestatus.MeldestatusResponse
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

fun Routing.meldekortApiV2(arenaOrdsService: ArenaOrdsService) {
    hentMeldekort(arenaOrdsService)
    hentHistoriskeMeldekort(arenaOrdsService)
    hentMeldekortdetaljer(arenaOrdsService)
    hentKorrigertMeldekort(arenaOrdsService)
    hentMeldegrupper(arenaOrdsService)
    hentMeldestatus(arenaOrdsService)
}

private const val group = "Meldekort v2"

@Group(group)
@Resource("$API_PATH/v2/meldekort")
class HentMeldekortInput

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
@Resource("$API_PATH/v2/historiskemeldekort")
class HentHistoriskeMeldekortInput(val antallMeldeperioder: Int)

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
@Resource("$API_PATH/v2/meldekortdetaljer")
data class HentMeldekortdetaljerInput(val meldekortId: Long)

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
@Resource("$API_PATH/v2/korrigertMeldekort")
data class HentKorrigertMeldekortInput(
    val meldekortId: Long
)

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
@Resource("$API_PATH/v2/meldegrupper")
class HentMeldegrupperInput

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

        val result = arenaOrdsService.hentMeldestatus(personIdent = ident)

        result.meldegruppeListe?.map { Meldegruppe(
            fodselsnr = ident,
            meldegruppeKode = it.meldegruppe,
            datoFra = it.meldegruppeperiode?.fom?.toLocalDate() ?: LocalDate.now(),
            datoTil = it.meldegruppeperiode?.tom?.toLocalDate(),
            hendelsesdato = LocalDate.now(),
            statusAktiv = "J",
            begrunnelse = it.begrunnelse,
            styrendeVedtakId = null
        ) } ?: emptyList<Meldegruppe>()
    }
}

@Group(group)
@Resource("$API_PATH/v2/meldestatus")
class MeldestatusInput

fun Routing.hentMeldestatus(arenaOrdsService: ArenaOrdsService) = post<MeldestatusInput, MeldestatusRequest>(
    "Hent meldestatus".securityAndResponse(
        BearerTokenSecurity(),
        ok<MeldestatusResponse>(),
        badRequest<ErrorMessage>(),
        serviceUnavailable<ErrorMessage>(),
        unAuthorized<Error>()
    ).header<Headers>(),
) { _, meldestatusRequest ->
    respondOrError {
        arenaOrdsService.hentMeldestatus(
            meldestatusRequest.arenaPersonId,
            meldestatusRequest.personident,
            meldestatusRequest.sokeDato
        )
    }
}

private fun getIdent(call: ApplicationCall): String {
    val ident = call.request.headers["ident"]
    if (ident.isNullOrBlank() || ident.length != 11) {
        throw SecurityException("Personidentifikator finnes ikke")
    }

    return ident
}
