package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.call.receive
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
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

class KontrollService {

    private val log = getLogger(KontrollService::class)

    private val env = Environment()

    private val kontrollClient: HttpClient = HttpClient() {
        engine {
            response.apply {
                charset(Charsets.UTF_8.displayName())
            }
        }
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }

    suspend fun ping(): OrdsStringResponse {
        val msg = kontrollClient.call("${env.kontrollUrl}$KONTROLL_KONTROLL") {
            setupKontrollRequest()
        }
        if (HTTP_STATUS_CODES_2XX.contains(msg.response.status.value)) {
            return OrdsStringResponse(msg.response.status, msg.response.receive())
        } else {
            throw OrdsException("Kunne ikke pinge meldekort-kontroll.")
        }
    }

    suspend fun hentMeldekort(fnr: String): OrdsStringResponse {
        val meldekort = kontrollClient.call("${env.ordsUrl}$KONTROLL_KONTROLL") {
            setupKontrollRequest()
        }
        if (HTTP_STATUS_CODES_2XX.contains(meldekort.response.status.value)) {
            return OrdsStringResponse(meldekort.response.status, meldekort.response.receive())
        } else {
            throw OrdsException("Kunne ikke hente meldekort fra Arena Ords.")
        }
    }

    private fun HttpRequestBuilder.setupKontrollRequest() {
        headers.append("Accept", "application/xml; charset=UTF-8")
        headers.append("Authorization","Bearer ${hentToken().accessToken}")
    }

    private fun hentToken(): OrdsToken {
        return cache.get("ordsToken", this::hentOrdsToken)
    }

    // TODO: Gå mot AAD
    private fun hentOrdsToken(): OrdsToken {
        log.info("Cache timet ut. Henter token")
        var token = OrdsToken(null, null, null)

        if (isCurrentlyRunningOnNais()) {
            runBlocking {
                token = kontrollClient.post("${env.ordsUrl}$ARENA_ORDS_TOKEN_PATH?grant_type=client_credentials") {
                    setupTokenRequest()
                }
            }
        } else {
            log.info("Henter ikke token da appen kjører lokalt")
            token = token.copy(accessToken = "token")
        }

        return token
    }

    // Denne bør være OK?
    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.ordsClientId}:${env.ordsClientSecret}"
        headers.append("Authorization", "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}")
    }
}