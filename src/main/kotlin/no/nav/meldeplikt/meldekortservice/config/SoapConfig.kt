package no.nav.meldeplikt.meldekortservice.config

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.nav.meldeplikt.meldekortservice.service.SoapService
import no.nav.meldeplikt.meldekortservice.service.SoapServiceImpl
import no.nav.meldeplikt.meldekortservice.service.SoapServiceMock
import no.nav.meldeplikt.meldekortservice.utils.SBL_ARBEID_PASSWORD
import no.nav.meldeplikt.meldekortservice.utils.SBL_ARBEID_USERNAME
import no.nav.meldeplikt.meldekortservice.utils.getLogger
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.sbl.dialogarena.common.cxf.CXFClient
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.apache.wss4j.dom.handler.WSHandlerConstants
import javax.security.auth.callback.CallbackHandler

object SoapConfig {

    private val log = getLogger(SoapConfig::class)

    private val environment = Environment()

    private val sblArbeidUsername = System.getProperty(SBL_ARBEID_USERNAME, "username")
    private val sblArbeidPassword = System.getProperty(SBL_ARBEID_PASSWORD, "password")

    private val interceptorConfig: Map<String, Any>
        get() {
            val map = java.util.HashMap<String, Any>()
            map[WSHandlerConstants.ACTION] = WSHandlerConstants.USERNAME_TOKEN
            map[WSHandlerConstants.PASSWORD_TYPE] = "PasswordText"
            map[WSHandlerConstants.USER] = sblArbeidUsername
            val passwordCallbackHandler = CallbackHandler { callbacks ->
                val callback = callbacks[0] as WSPasswordCallback
                callback.password = sblArbeidPassword
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
        log.info("Kjører på nais. Setter opp SoapService. SBLArbeidBrukernavn: $sblArbeidUsername/$sblArbeidPassword")
        return CXFClient(ExternControlEmeldingSOAP::class.java)
            .address(environment.ameldingUrl.toString())
            .withOutInterceptor(WSS4JOutInterceptor(interceptorConfig))
            .build()
    }

    fun sakOgAktivitet(): CXFClient<SakOgAktivitetV1> {
        val url = environment.sakOgAktivitetUrl
        log.info("URL for Oppfoelging_V1 er $url")
        return CXFClient(SakOgAktivitetV1::class.java)
            .withOutInterceptor(LoggingOutInterceptor())
            .address(url)
    }
}