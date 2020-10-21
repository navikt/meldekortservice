package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.api.jsonMapper
import no.nav.meldeplikt.meldekortservice.config.AadServiceConfiguration
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.mapper.KontrollertTypeMapper
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.*
import kotlin.math.log

class KontrollService {

    private val env = Environment()
    private val responseMapper = KontrollertTypeMapper()
    private val aadService = AadService(AadServiceConfiguration())

    suspend fun kontroller(meldekort: Meldekortkontroll): MeldekortKontrollertType {
        val tokenInfo = kontrollClient.get<String> {
            url("${env.meldekortKontrollUrl}$KONTROLL_TOKENINFO")
            contentType(ContentType.Application.Json)
            header("Authorization", aadService.hentAadToken())
        }
        defaultLog.info("Tokeninfo: "+ tokenInfo)
        val message = kontrollClient.post<KontrollResponse> {
            url("${env.meldekortKontrollUrl}$KONTROLL_KONTROLL")
            contentType(ContentType.Application.Json)
            header("Authorization", aadService.hentAadToken())
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
            serializer = JacksonSerializer { objectMapper }
        }
    }

}