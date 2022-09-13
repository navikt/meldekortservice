package no.nav.meldeplikt.meldekortservice.config

import no.nav.common.cxf.CXFClient
import no.nav.meldeplikt.meldekortservice.service.SoapService
import no.nav.meldeplikt.meldekortservice.service.SoapServiceImpl
import no.nav.meldeplikt.meldekortservice.service.SoapServiceMock
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1
import org.apache.cxf.ext.logging.LoggingOutInterceptor

object SoapConfig {

    private val environment = Environment()

    // Velger hvilken Soapservice som skal returneres, avhengig av om appen kjører på nais eller ikke
    fun soapService(): SoapService {
        return if(isCurrentlyRunningOnNais()) {
            SoapServiceImpl()
        } else {
            SoapServiceMock()
        }
    }

    fun sakOgAktivitet(): CXFClient<SakOgAktivitetV1> {
        val url = environment.sakOgAktivitetUrl
        return CXFClient(SakOgAktivitetV1::class.java)
            .withOutInterceptor(LoggingOutInterceptor())
            .address(url)
    }
}
