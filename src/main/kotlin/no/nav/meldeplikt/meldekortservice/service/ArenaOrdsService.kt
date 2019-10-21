package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.cache
import no.nav.meldeplikt.meldekortservice.config.client
import no.nav.meldeplikt.meldekortservice.model.OrdsToken
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.utils.ARENA_ORDS_HENT_MELDEKORT
import no.nav.meldeplikt.meldekortservice.utils.ARENA_ORDS_TOKEN_PATH
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import java.util.*

object ArenaOrdsService {

    private val env = Environment()
    private val xmlMapper = XmlMapper()

   /* init {
        xmlMapper.propertyNamingStrategy = PropertyNamingStrategy.UPPER_CAMEL_CASE
    }*/

    fun hentMeldekort(fnr: String): Person {
        val person = runBlocking {
            client.get<String>("${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORT$fnr") {
                setupOrdsRequest()
            }
        }
        println(person)
        return xmlMapper.readValue(person, Person::class.java)
    }

    fun hentToken(): OrdsToken {
        return cache.get("ordsToken", this::hentOrdsToken)
    }

    private fun hentOrdsToken(): OrdsToken {
        println("Cache timet ut. Henter token")
        var token = OrdsToken(null, null, null)

        if (isCurrentlyRunningOnNais()) {
            runBlocking {
                token = client.post("${env.ordsUrl}$ARENA_ORDS_TOKEN_PATH?grant_type=client_credentials") {
                    setupTokenRequest()
                }
            }
        } else {
            println("Henter ikke token da appen kjører lokalt")
            token = token.copy(accessToken = "token")
        }

        return token
    }

    private fun HttpRequestBuilder.setupOrdsRequest() {
        headers.append("Accept", "application/xml; charset=UTF-8")
        headers.append("Authorization","Bearer ${hentToken().accessToken}")
    }

    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.ordsClientId}:${env.ordsClientSecret}"
        headers.append("Authorization", "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}")
    }
}