package no.nav.meldeplikt.meldekortservice.config

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.nav.meldeplikt.meldekortservice.service.SoapService
import no.nav.meldeplikt.meldekortservice.service.SoapServiceImpl
import no.nav.meldeplikt.meldekortservice.service.SoapServiceMock
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.sbl.dialogarena.common.cxf.CXFClient
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.apache.wss4j.dom.handler.WSHandlerConstants
import javax.security.auth.callback.CallbackHandler

object SoapConfig {

    private val environment = Environment()

    private val interceptorConfig: Map<String, Any>
        get() {
            val map = java.util.HashMap<String, Any>()
            map[WSHandlerConstants.ACTION] = WSHandlerConstants.USERNAME_TOKEN
            map[WSHandlerConstants.PASSWORD_TYPE] = "PasswordText"
            map[WSHandlerConstants.USER] = environment.personinfoUsername
            val passwordCallbackHandler = CallbackHandler { callbacks ->
                val callback = callbacks[0] as WSPasswordCallback
                callback.password = environment.personinfoPassword
            }
            map[WSHandlerConstants.PW_CALLBACK_REF] = passwordCallbackHandler
            return map
        }

    //Velger hvilke av Ameldingsservicene som skal returneres ettersom om appen kjører på nais eller ikke
    fun soapService(): SoapService {
        return if(isCurrentlyRunningOnNais()) {
            SoapServiceImpl(externControlEmeldingConfig())
        } else {
            SoapServiceMock()
        }
    }

    //Setter opp tilkobling mot Amelding
    private fun externControlEmeldingConfig(): ExternControlEmeldingSOAP {
        return CXFClient(ExternControlEmeldingSOAP::class.java)
            .address(environment.ameldingUrl.toString())
            .withOutInterceptor(WSS4JOutInterceptor(interceptorConfig))
            .build()
    }

    fun sakOgAktivitet(): CXFClient<SakOgAktivitetV1> {
        val url = environment.sakOgAktivitetUrl
        println("URL for Oppfoelging_V1 er $url")
        return CXFClient(SakOgAktivitetV1::class.java)
            .withOutInterceptor(LoggingOutInterceptor())
            .address(url)
    }
}