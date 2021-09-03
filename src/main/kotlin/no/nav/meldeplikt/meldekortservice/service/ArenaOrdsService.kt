package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.CACHE
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
import java.util.*

class ArenaOrdsService(
    private val ordsClient: HttpClient = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer { objectMapper }
        }
    },
    private val env: Environment = Environment()
) {
    private val log = getLogger(ArenaOrdsService::class)

    suspend fun hentMeldekort(fnr: String): OrdsStringResponse {
        val execResult: Result<HttpResponse> = runCatching {
            ordsClient.request("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORT$fnr") {
                setupOrdsRequest()
            }
        }

        val meldekort = execResult.getOrNull()
        if (execResult.isFailure || !HTTP_STATUS_CODES_2XX.contains(meldekort!!.status.value)) {
            throw OrdsException("Kunne ikke hente meldekort fra Arena Ords.")
        }

        return OrdsStringResponse(meldekort.status, meldekort.receive())
    }

    suspend fun hentHistoriskeMeldekort(fnr: String, antallMeldeperioder: Int): Person {
        val person = ordsClient.get<String>(
            "${env.ordsUrl}$ARENA_ORDS_HENT_HISTORISKE_MELDEKORT$fnr" +
                    "$ARENA_ORDS_MELDEPERIODER_PARAM$antallMeldeperioder"
        ) {
            setupOrdsRequest()
        }

        return mapFraXml(person, Person::class.java)
    }

    suspend fun hentMeldekortdetaljer(meldekortId: Long): Meldekortdetaljer {
        val detaljer = ordsClient.get<String>("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORTDETALJER$meldekortId") {
            setupOrdsRequest()
        }

        return MeldekortdetaljerMapper.mapOrdsMeldekortTilMeldekortdetaljer(mapFraXml(detaljer, Meldekort::class.java))
    }

    suspend fun kopierMeldekort(meldekortId: Long): Long {
        val nyMeldekortId = ordsClient.post<String>("${env.ordsUrl}$ARENA_ORDS_KOPIER_MELDEKORT") {
            setupOrdsRequest(meldekortId)
        }

        return mapFraXml(nyMeldekortId, KopierMeldekortResponse::class.java).meldekortId
    }

    private fun HttpRequestBuilder.setupOrdsRequest(meldekortId: Long? = null) {
        headers.append("Accept", "application/xml; charset=UTF-8")
        headers.append("Authorization", "Bearer ${hentToken().accessToken}")
        if (meldekortId != null) {
            headers.append("meldekortId", meldekortId.toString())
        }
    }

    private fun hentToken(): AccessToken {
        return CACHE.get("ordsToken", this::hentOrdsToken)
    }

    private fun hentOrdsToken(): AccessToken {
        log.info("Cache timet ut. Henter token")
        var token = AccessToken(null, null, null)

        if (isCurrentlyRunningOnNais()) {
            runBlocking {
                token = ordsClient.post("${env.ordsUrl}$ARENA_ORDS_TOKEN_PATH?grant_type=client_credentials") {
                    setupTokenRequest()
                }
            }
        } else {
            log.info("Henter ikke token da appen kjører lokalt")
            token = token.copy(accessToken = "token")
        }

        return token
    }

    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.ordsClientId}:${env.ordsClientSecret}"
        headers.append("Authorization", "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}")
    }
}