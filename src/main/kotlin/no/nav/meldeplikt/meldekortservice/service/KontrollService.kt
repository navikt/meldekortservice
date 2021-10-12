package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.AadServiceConfiguration
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.mapper.KontrollertTypeMapper
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.KONTROLL_KONTROLL
import no.nav.meldeplikt.meldekortservice.utils.objectMapper

class KontrollService(
    private val env: Environment = Environment(),
    private val responseMapper: KontrollertTypeMapper = KontrollertTypeMapper(),
    private val aadService: AadService = AadService(AadServiceConfiguration()),
    private val kontrollClient: HttpClient = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer { objectMapper }
        }
    }
) {
    suspend fun kontroller(meldekort: Meldekortkontroll): MeldekortKontrollertType {
        val message = kontrollClient.post<KontrollResponse> {
            url("${env.meldekortKontrollUrl}$KONTROLL_KONTROLL")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer " + aadService.fetchAadToken())
            body = meldekort
        }
        return responseMapper.mapKontrollResponseToKontrollertType(message)
    }
}