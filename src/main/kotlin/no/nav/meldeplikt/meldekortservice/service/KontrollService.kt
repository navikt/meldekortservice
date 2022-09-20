package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.meldeplikt.meldekortservice.config.AadServiceConfiguration
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.mapper.KontrollertTypeMapper
import no.nav.meldeplikt.meldekortservice.model.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.KONTROLL_KONTROLL
import no.nav.meldeplikt.meldekortservice.utils.defaultHttpClient

class KontrollService(
    private val env: Environment = Environment(),
    private val responseMapper: KontrollertTypeMapper = KontrollertTypeMapper(),
    private val aadService: AadService = AadService(AadServiceConfiguration()),
    private val kontrollClient: HttpClient = defaultHttpClient()
) {
    suspend fun kontroller(meldekort: Meldekortkontroll): MeldekortKontrollertType {
        val message: KontrollResponse = kontrollClient.post("${env.meldekortKontrollUrl}$KONTROLL_KONTROLL") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer " + aadService.fetchAadToken())
            setBody(meldekort)
        }.body()
        return responseMapper.mapKontrollResponseToKontrollertType(message)
    }
}
