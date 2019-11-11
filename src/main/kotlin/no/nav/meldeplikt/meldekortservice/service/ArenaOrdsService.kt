package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.call.receive
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.request
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
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.Meldekort
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import java.util.*

object ArenaOrdsService {

    private val log = getLogger(ArenaOrdsService::class)

    private val env = Environment()
    private val xmlMapper = XmlMapper()

    private fun ordsClient() = HttpClient() {
        engine {
            response.apply {
                charset(Charsets.UTF_8.displayName())
            }
        }
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }

    fun hentMeldekort(fnr: String): OrdsStringResponse = runBlocking {
        val meldekort = ordsClient().call("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORT$fnr") {
            setupOrdsRequest()
        }
        if (HTTP_STATUS_CODES_2XX.contains(meldekort.response.status.value)) {
            OrdsStringResponse(meldekort.response.status, meldekort.response.receive())
        } else {
            throw OrdsException("Kunne ikke hente meldekort fra Arena Ords.")
        }
    }

    fun hentHistoriskeMeldekort(fnr: String, antallMeldeperioder: Int): Person {
        val person = runBlocking {
            ordsClient().get<String>(
                "${env.ordsUrl}$ARENA_ORDS_HENT_HISTORISKE_MELDEKORT$fnr" +
                        "$ARENA_ORDS_MELDEPERIODER_PARAM$antallMeldeperioder"
            ) {
                setupOrdsRequest()
            }
        }
        return xmlMapper.readValue(person, Person::class.java)
    }

    fun hentMeldekortdetaljer(meldekortId: Long): Meldekortdetaljer {
        val detaljer = runBlocking {
            ordsClient().get<String>("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORTDETALJER$meldekortId") {
                setupOrdsRequest()
            }
        }
        val meldekort = xmlMapper.readValue(detaljer, Meldekort::class.java)
        return MeldekortdetaljerMapper.mapOrdsMeldekortTilMeldekortdetaljer(meldekort)
    }

    fun kopierMeldekort(meldekortId: Long): Long {
        val nyMeldekortId = runBlocking {
            ordsClient().post<String>("${env.ordsUrl}$ARENA_ORDS_KOPIER_MELDEKORT") {
                setupOrdsRequest(meldekortId)
            }
        }
        val response = xmlMapper.readValue(nyMeldekortId, KopierMeldekortResponse::class.java)
        return response.meldekortId
    }

    fun endreMeldeform(fnr: String, meldeformNavn: String): Meldeperiode {
        val meldeperiodeResponse = runBlocking {
            ordsClient().post<String>("${env.ordsUrl}$ARENA_ORDS_ENDRE_MELDEFORM") {
                    setupOrdsRequest()
                    headers.append("fnr", fnr)
                    headers.append("meldeform", meldeformNavn)
            }
        }
        return xmlMapper.readValue(meldeperiodeResponse, Meldeperiode::class.java)

    }

    private fun HttpRequestBuilder.setupOrdsRequest(meldekortId: Long? = null) {
        headers.append("Accept", "application/xml; charset=UTF-8")
        headers.append("Authorization","Bearer ${hentToken().accessToken}")
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
                token = ordsClient().post("${env.ordsUrl}$ARENA_ORDS_TOKEN_PATH?grant_type=client_credentials") {
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