package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.cache
import no.nav.meldeplikt.meldekortservice.mapper.KontrollertTypeMapper
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortkontrollMapper
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.utils.*
import java.util.*

class KontrollService {

    private val log = getLogger(KontrollService::class)
    private val env = Environment()
    private val responseMapper = KontrollertTypeMapper()

    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }

    suspend fun kontroller(meldekort: Meldekortkontroll): MeldekortKontrollertType {
        val message = client.post<KontrollResponse> {
            url("${env.kontrollUrl}$KONTROLL_KONTROLL")
            contentType(ContentType.Application.Json)
            body = meldekort
        }
        defaultLog.info(message.toString())

        return responseMapper.mapKontrollResponseToKontrollertType(message)
    }

    private val kontrollClient: HttpClient = HttpClient {
        engine {
            response.apply {
                charset(Charsets.UTF_8.displayName())
            }
        }
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }

//    private fun setupKontrollRequest(meldekortdetaljer: Meldekortdetaljer): HttpRequestBuilder {
//        val req = HttpRequestBuilder()
//        req.headers.append("Accept", "application/xml; charset=UTF-8")
//        req.headers.append("Authorization","Bearer ${hentToken().accessToken}")
//        req.method=HttpMethod.Post
//        req.body=meldekortdetaljer
//        return req
//    }

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