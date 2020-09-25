package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.api.MeldekortInput
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.cache
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.utils.*
import java.util.*

class KontrollService {

    private val log = getLogger(KontrollService::class)

    private val env = Environment()

    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }

    suspend fun kontroller(meldekort: Meldekortkontroll): String {
        val message = client.post<Meldekortdetaljer> {
            url("${env.kontrollUrl}$KONTROLL_KONTROLL")
            contentType(ContentType.Application.Json)
            body = meldekort
        }
        return message.toString()
    }

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

//    suspend fun ping(): OrdsStringResponse {
//        val msg = kontrollClient.call("${env.kontrollUrl}$KONTROLL_KONTROLL") {
//            setupKontrollRequestPing()
//        }
//        if (HTTP_STATUS_CODES_2XX.contains(msg.response.status.value)) {
//            return OrdsStringResponse(msg.response.status, msg.response.receive())
//        } else {
//            throw OrdsException("Kunne ikke pinge meldekort-kontroll.")
//        }
//    }

//    suspend fun kontroll(meldekortdetaljer: Meldekortdetaljer): String {
// //       try {
//        val req = HttpRequestBuilder()
//        req.headers.append("Accept", "application/xml; charset=UTF-8")
//        //req.headers.append("Authorization","Bearer ${hentToken().accessToken}")
//        req.method=HttpMethod.Post
//        req.url = URLBuilder( Url("${env.kontrollUrl}$KONTROLL_KONTROLL"))
//        req.body=meldekortdetaljer
//        val msg = kontrollClient.request<String>(req)
//        return msg
// //       } catch(e: Exception) {
// //           throw Exception(e)
// //       }
//    }

    private fun setupKontrollRequest(meldekortdetaljer: Meldekortdetaljer): HttpRequestBuilder {
        val req = HttpRequestBuilder()
        req.headers.append("Accept", "application/xml; charset=UTF-8")
        req.headers.append("Authorization","Bearer ${hentToken().accessToken}")
        req.method=HttpMethod.Post
        req.body=meldekortdetaljer
        return req
    }

//    private fun HttpRequestBuilder.setupKontrollRequestPing() {
//        headers.append("Accept", "application/xml; charset=UTF-8")
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