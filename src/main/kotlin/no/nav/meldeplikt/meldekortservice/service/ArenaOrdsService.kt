package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.cache
import no.nav.meldeplikt.meldekortservice.config.client
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.Meldekort
import no.nav.meldeplikt.meldekortservice.utils.*
import java.util.*

object ArenaOrdsService {

    private val log = getLogger(ArenaOrdsService::class)

    private val env = Environment()
    private val xmlMapper = XmlMapper()

    fun hentMeldekort(fnr: String): Person {
        val person = runBlocking {
            client.get<String>("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORT$fnr") {
                setupOrdsRequest()
            }
        }
        return xmlMapper.readValue(person, Person::class.java)
    }

    fun hentHistoriskeMeldekort(fnr: String, antallMeldeperioder: Int): Person {
        val person = runBlocking {
            client.get<String>(
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
            client.get<String>("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORTDETALJER$meldekortId") {
                setupOrdsRequest()
            }
        }
        val meldekort = xmlMapper.readValue(detaljer, Meldekort::class.java)
        log.info("Hent meldekortdetaljer. Meldekortdetaljer var: $meldekort")
        return Meldekortdetaljer(kortType = KortType.AAP)
    }
z
    fun kopierMeldekort(meldekortId: Long): Long {
        val nyMeldekortId = runBlocking {
            client.get<Long>("${env.ordsUrl}$ARENA_ORDS_KOPIER_MELDEKORT$meldekortId") {
                setupTokenRequest()
            }
        }
        return nyMeldekortId
    }

    private fun HttpRequestBuilder.setupOrdsRequest() {
        headers.append("Accept", "application/xml; charset=UTF-8")
        headers.append("Authorization","Bearer ${hentToken().accessToken}")
    }

    private fun hentToken(): OrdsToken {
        return cache.get("ordsToken", this::hentOrdsToken)
    }

    private fun hentOrdsToken(): OrdsToken {
        log.info("Cache timet ut. Henter token")
        var token = OrdsToken(null, null, null)

        if (isCurrentlyRunningOnNais()) {
            runBlocking {
                token = client.post("${env.ordsUrl}$ARENA_ORDS_TOKEN_PATH?grant_type=client_credentials") {
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