package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.util.StringValuesBuilder
import io.ktor.util.appendAll
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.DUMMY_TOKEN
import no.nav.meldeplikt.meldekortservice.config.DUMMY_URL
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortdetaljerMapper
import no.nav.meldeplikt.meldekortservice.model.AccessToken
import no.nav.meldeplikt.meldekortservice.model.ArenaOrdsSkrivemodus
import no.nav.meldeplikt.meldekortservice.model.feil.OrdsException
import no.nav.meldeplikt.meldekortservice.model.korriger.KopierMeldekortResponse
import no.nav.meldeplikt.meldekortservice.model.meldegruppe.MeldegruppeResponse
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Meldekort
import no.nav.meldeplikt.meldekortservice.model.meldestatus.MeldestatusRequest
import no.nav.meldeplikt.meldekortservice.model.meldestatus.MeldestatusResponse
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ArenaOrdsService(
    private val ordsClient: HttpClient = defaultHttpClient(),
    private val env: Environment = Environment()
) {
    private lateinit var currentToken: AccessToken
    private lateinit var currentTokenValidUntil: LocalDateTime

    suspend fun hentMeldekort(ident: String): OrdsStringResponse {
        val execResult: Result<HttpResponse> = runCatching {
            getResponseWithRetry(
                "${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORT",
                HttpMethod.Get,
                setupHeaders(ident = ident)
            )
        }

        val meldekort = execResult.getOrNull()
        if (execResult.isFailure || !HTTP_STATUS_CODES_2XX.contains(meldekort!!.status.value)) {
            throw OrdsException("Kunne ikke hente meldekort fra Arena Ords.")
        }

        return OrdsStringResponse(meldekort.status, meldekort.body())
    }

    suspend fun hentHistoriskeMeldekort(ident: String, antallMeldeperioder: Int): Person {
        val person: String = getResponseWithRetry(
            "${env.ordsUrl}$ARENA_ORDS_HENT_HISTORISKE_MELDEKORT$ARENA_ORDS_MELDEPERIODER_PARAM$antallMeldeperioder",
            HttpMethod.Get,
            setupHeaders(ident = ident)
        ).body()

        return mapFraXml(person, Person::class.java)
    }

    suspend fun hentMeldekortdetaljer(meldekortId: Long): Meldekortdetaljer {
        val detaljer: String = getResponseWithRetry(
            "${env.ordsUrl}$ARENA_ORDS_HENT_MELDEKORTDETALJER$meldekortId",
            HttpMethod.Get,
            setupHeaders()
        ).body()

        return MeldekortdetaljerMapper.mapOrdsMeldekortTilMeldekortdetaljer(mapFraXml(detaljer, Meldekort::class.java))
    }

    suspend fun kopierMeldekort(meldekortId: Long): Long {
        try {
            val responseMedNyMeldekortId: String = getResponseWithRetry(
                "${env.ordsUrl}$ARENA_ORDS_KOPIER_MELDEKORT",
                HttpMethod.Post,
                setupHeaders(meldekortId = meldekortId)
            ).body()

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

    suspend fun hentMeldegrupper(ident: String, fraDato: LocalDate): MeldegruppeResponse {
        val personId: String

        val personResponse = hentMeldekort(ident)
        if (personResponse.status == HttpStatusCode.OK) {
            val person = mapFraXml(personResponse.content, Person::class.java)
            personId = person.personId.toString()
        } else {
            return MeldegruppeResponse(emptyList())
        }

        val response = getResponseWithRetry(
            "${env.ordsUrl}$ARENA_ORDS_HENT_MELDEGRUPPER",
            HttpMethod.Get,
            setupHeaders(personId = personId, fraDato = fraDato)
        )

        if (response.status != HttpStatusCode.OK) {
            throw OrdsException("Kunne ikke hente meldegrupper fra Arena Ords")
        }

        return defaultObjectMapper.readValue(response.body<String>(), MeldegruppeResponse::class.java)
    }

    suspend fun hentMeldestatus(
        arenaPersonId: Long? = null,
        personIdent: String? = null,
        sokeDato: LocalDate? = null
    ): MeldestatusResponse {
        val headers = HeadersBuilder()
        headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
        headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        headers.append("Authorization", "Bearer ${hentToken().accessToken}")

        val response = getResponseWithRetry(
            "${env.ordsUrl}$ARENA_ORDS_HENT_MELDESTATUS",
            HttpMethod.Post,
            headers,
            defaultObjectMapper.writeValueAsString(MeldestatusRequest(arenaPersonId, personIdent, sokeDato))
        )

        if (response.status != HttpStatusCode.OK) {
            throw OrdsException("Kunne ikke hente meldestatus fra Arena Ords")
        }

        return defaultObjectMapper.readValue(response.body<String>(), MeldestatusResponse::class.java)
    }

    suspend fun hentSkrivemodus(): ArenaOrdsSkrivemodus {
        val execResult: Result<HttpResponse> = runCatching {
            getResponseWithRetry("${env.ordsUrl}$ARENA_ORDS_HENT_SKRIVEMODUS", HttpMethod.Get, setupHeaders())
        }

        val response = execResult.getOrNull()

        if (execResult.isFailure || !HTTP_STATUS_CODES_2XX.contains(response!!.status.value)) {
            return ArenaOrdsSkrivemodus(false)
        }

        val content =
            defaultObjectMapper.readValue(response.body<String>(), ArenaOrdsSkrivemodus::class.java)

        return ArenaOrdsSkrivemodus(content.skrivemodus)
    }

    private fun setupHeaders(
        meldekortId: Long? = null,
        ident: String? = null,
        personId: String? = null,
        fraDato: LocalDate? = null
    ): StringValuesBuilder {
        val headers = HeadersBuilder()
        headers.append("Accept", "application/xml; charset=UTF-8")
        headers.append("Authorization", "Bearer ${hentToken().accessToken}")
        if (meldekortId != null) {
            headers.append("meldekortId", meldekortId.toString())
        }
        if (ident != null) {
            headers.append("fnr", ident)
        }
        if (personId != null && fraDato != null) {
            headers.append("personid", personId)
            headers.append("fradato", fraDato.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        return headers
    }


    private suspend fun getResponseWithRetry(
        url: String,
        httpMethod: HttpMethod,
        httpHeaders: StringValuesBuilder,
        body: String? = null,
    ): HttpResponse {
        var response = getResponse(url, httpMethod, httpHeaders, body)

        if (response.status == HttpStatusCode.Unauthorized) {
            hentOrdsToken()
            response = getResponse(url, httpMethod, httpHeaders, body)
        }

        return response
    }

    private suspend fun getResponse(
        url: String,
        httpMethod: HttpMethod,
        httpHeaders: StringValuesBuilder,
        body: String? = null,
    ): HttpResponse {
        return ordsClient.request(url) {
            method = httpMethod
            headers.appendAll(httpHeaders)
            if (body != null) {
                this.setBody(body)
            }
        }
    }

    private fun hentToken(): AccessToken {
        if (!::currentToken.isInitialized || LocalDateTime.now().isAfter(currentTokenValidUntil.minusMinutes(5))) {
            hentOrdsToken()
        }

        return currentToken
    }

    private fun hentOrdsToken() {
        defaultLog.debug("Henter ORDS-token")
        var token = AccessToken(null, null, null)

        if (env.ordsUrl != DUMMY_URL) {
            runBlocking {
                val response = ordsClient.post("${env.ordsUrl}$ARENA_ORDS_TOKEN_PATH?grant_type=client_credentials") {
                    setupTokenRequest()
                }

                token = defaultObjectMapper.readValue(response.bodyAsText())
            }
        } else {
            defaultLog.info("Henter ikke ORDS-token, da appen kjører lokalt")
            token = AccessToken(
                accessToken = DUMMY_TOKEN,
                tokenType = "bearer",
                expiresIn = 3600
            )
        }

        currentToken = token
        currentTokenValidUntil = LocalDateTime.now().plusSeconds(token.expiresIn?.toLong() ?: 0)
    }

    private fun HttpRequestBuilder.setupTokenRequest() {
        val base = "${env.ordsClientId}:${env.ordsClientSecret}"
        headers.append("Accept", "application/json; charset=UTF-8")
        headers.append("Authorization", "Basic ${Base64.getEncoder().encodeToString(base.toByteArray())}")
    }
}
