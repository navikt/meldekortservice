package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.cache
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortdetaljerMapper
import no.nav.meldeplikt.meldekortservice.model.Meldeperiode
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.model.feil.OrdsException
import no.nav.meldeplikt.meldekortservice.model.korriger.KopierMeldekortResponse
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Meldekort
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import java.util.*

class ArenaOrdsService {

    private val log = getLogger(ArenaOrdsService::class)

    private val env = Environment()

    private val ordsClient: HttpClient = HttpClient {
        engine {
            response.apply {
                charset(Charsets.UTF_8.displayName())
            }
        }
        install(JsonFeature) {
            serializer = JacksonSerializer { objectMapper }
        }
    }

    suspend fun hentMeldekort(fnr: String): OrdsStringResponse {
        val meldekort = ordsClient.call("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORT$fnr") {
            setupOrdsRequest()
        }
        if (HTTP_STATUS_CODES_2XX.contains(meldekort.response.status.value)) {
            return OrdsStringResponse(meldekort.response.status, meldekort.response.receive())
        } else {
            throw OrdsException("Kunne ikke hente meldekort fra Arena Ords.")
        }
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

    suspend fun endreMeldeform(fnr: String, meldeformNavn: String): Meldeperiode {
        val meldeperiodeResponse = ordsClient.post<String>("${env.ordsUrl}$ARENA_ORDS_ENDRE_MELDEFORM") {
            setupOrdsRequest()
            headers.append("fnr", fnr)
            headers.append("meldeform", meldeformNavn)
        }

        return mapFraXml(meldeperiodeResponse, Meldeperiode::class.java)
    }

    private fun HttpRequestBuilder.setupOrdsRequest(meldekortId: Long? = null) {
        headers.append("Accept", "application/xml; charset=UTF-8")
        headers.append("Authorization", "Bearer ${hentToken().accessToken}")
        if (meldekortId != null) {
            headers.append("meldekortId", meldekortId.toString())
        }
    }

    private fun hentToken(): OrdsToken {
        return cache.get("ordsToken", this::hentOrdsToken)
    }

    private fun hentOrdsToken(): OrdsToken {
        log.info("Cache timet ut. Henter token")
        var token = OrdsToken(null, null, null)

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