package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.AadServiceConfiguration
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.mapper.KontrollertTypeMapper
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector


class KontrollService {

//    private val mapper = ObjectMapper()

    private val log = getLogger(KontrollService::class)
    private val env = Environment()
    private val responseMapper = KontrollertTypeMapper()
    private val aadService = AadService(AadServiceConfiguration())

    suspend fun kontroller(meldekort: Meldekortkontroll): MeldekortKontrollertType {
//        log.info("Header: "+hentAadToken().accessToken)
        val message = kontrollClient.post<KontrollResponse> {
            url("${env.meldekortKontrollUrl}$KONTROLL_KONTROLL")
            contentType(ContentType.Application.Json)
            header("Authorization", aadService.hentAadToken().accessToken)
            body = meldekort
        }
//        defaultLog.info(message.toString())
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

}