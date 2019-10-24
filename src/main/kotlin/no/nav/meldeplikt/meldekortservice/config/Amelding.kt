package no.nav.meldeplikt.meldekortservice.config

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.nav.meldeplikt.meldekortservice.service.AmeldingService
import no.nav.meldeplikt.meldekortservice.service.AmeldingServiceImpl
import no.nav.meldeplikt.meldekortservice.service.AmeldingServiceMock
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.sbl.dialogarena.common.cxf.CXFClient
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.apache.wss4j.dom.handler.WSHandlerConstants
import javax.security.auth.callback.CallbackHandler

object Amelding {

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
    fun ameldingService(): AmeldingService {
        return if(isCurrentlyRunningOnNais()) {
            AmeldingServiceImpl(externControlEmeldingConfig())
        } else {
            AmeldingServiceMock()
        }
    }

    //Setter opp tilkobling mot Amelding
    private fun externControlEmeldingConfig(): ExternControlEmeldingSOAP {
        return CXFClient(ExternControlEmeldingSOAP::class.java)
            .address(environment.ameldingUrl.toString())
            .withOutInterceptor(WSS4JOutInterceptor(interceptorConfig))
            .build()
    }
}