package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.DUMMY_TOKEN
import no.nav.meldeplikt.meldekortservice.config.DUMMY_URL
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortdetaljerMapper
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.model.feil.OrdsException
import no.nav.meldeplikt.meldekortservice.model.korriger.KopierMeldekortResponse
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Meldekort
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import java.net.URL
import java.util.*

class ArenaOrdsService(
    private val ordsClient: HttpClient = defaultHttpClient(),
    private val env: Environment = Environment()
) {

    suspend fun hentMeldekort(fnr: String): OrdsStringResponse {
        val execResult: Result<HttpResponse> = runCatching {
            ordsClient.request("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORT") {
                setupOrdsRequestFnr(fnr)
            }
        }

        val meldekort = execResult.getOrNull()
        if (execResult.isFailure || !HTTP_STATUS_CODES_2XX.contains(meldekort!!.status.value)) {
            throw OrdsException("Kunne ikke hente meldekort fra Arena Ords.")
        }

        return OrdsStringResponse(meldekort.status, meldekort.body())
    }

    suspend fun hentHistoriskeMeldekort(fnr: String, antallMeldeperioder: Int): Person {
        val person: String = ordsClient.get(
            "${env.ordsUrl}$ARENA_ORDS_HENT_HISTORISKE_MELDEKORT" +
                    "$ARENA_ORDS_MELDEPERIODER_PARAM$antallMeldeperioder"
        ) {
            setupOrdsRequestFnr(fnr)
        }.body()

        return mapFraXml(person, Person::class.java)
    }

    suspend fun hentMeldekortdetaljer(meldekortId: Long): Meldekortdetaljer {
        val detaljer: String = ordsClient.get("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORTDETALJER$meldekortId") {
            setupOrdsRequest()
        }.body()

        return MeldekortdetaljerMapper.mapOrdsMeldekortTilMeldekortdetaljer(mapFraXml(detaljer, Meldekort::class.java))
    }

    suspend fun kopierMeldekort(meldekortId: Long): Long {
        try {
            val responseMedNyMeldekortId: String = ordsClient.post("${env.ordsUrl}$ARENA_ORDS_KOPIER_MELDEKORT") {
                setupOrdsRequest(meldekortId)
            }.body()

            val nyMeldekortId = mapFraXml(responseMedNyMeldekortId, KopierMeldekortResponse::class.java).meldekortId
            defaultLog.info("Meldekort med id $nyMeldekortId er opprettet for korrigering. Kopiert fra meldekort med id $meldekortId")
            return nyMeldekortId

        } catch (e: Exception) {
            defaultLog.warn(
                "Feil ved opprettelse av meldekort for korrigering! Meldekort med id $meldekortId har ikke blitt kopiert.",
                e
            )
        }

        return 0
    }

    suspend fun hentSkrivemodus(): OrdsStringResponse {
        val execResult: Result<HttpResponse> = runCatching {
            ordsClient.request("${env.ordsUrl}$ARENA_ORDS_HENT_SKRIVEMODUS") {
                setupOrdsRequest()
            }
        }

        val skrivemodus = execResult.getOrNull()
        if (execResult.isFailure || !HTTP_STATUS_CODES_2XX.contains(skrivemodus!!.status.value)) {
            throw OrdsException("Kunne ikke hente skrivemodus fra Arena Ords.")
        }

        return OrdsStringResponse(skrivemodus.status, skrivemodus.body())
    }


    private fun HttpRequestBuilder.setupOrdsRequestFnr(fnr: String? = null) {
        return setupOrdsRequest(null, fnr)
    }

    private fun HttpRequestBuilder.setupOrdsRequest(meldekortId: Long? = null, fnr: String? = null) {
        headers.append("Accept", "application/xml; charset=UTF-8")
        headers.append("Authorization", "Bearer ${hentToken().accessToken}")
        if (meldekortId != null) {
            headers.append("meldekortId", meldekortId.toString())
        }
        if (fnr != null) {
            headers.append("fnr", fnr)
        }
    }

    private fun hentToken(): AccessToken {
        return CACHE.get("ordsToken", this::hentOrdsToken)
    }

    private fun hentOrdsToken(): AccessToken {
        defaultLog.debug("Henter ORDS-token")
        var token = AccessToken(null, null, null)

        if (env.ordsUrl != URL(DUMMY_URL)) {
            runBlocking {
                token = ordsClient.post("${env.ordsUrl}$ARENA_ORDS_TOKEN_PATH?grant_type=client_credentials") {
                    setupTokenRequest()
                }.body()
            }
        } else {
            defaultLog.info("Henter ikke ORDS-token, da appen kjører lokalt")
            token = AccessToken(
                accessToken = DUMMY_TOKEN,
                tokenType = "bearer",
                expiresIn = 3600
            )
        }

        return token
    }

    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.ordsClientId}:${env.ordsClientSecret}"
        headers.append("Accept", "application/json; charset=UTF-8")
        headers.append("Authorization", "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}")
    }
}
