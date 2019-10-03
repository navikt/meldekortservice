package no.nav.meldeplikt.meldekortservice.config

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.nav.meldeplikt.meldekortservice.service.AmeldingService
import no.nav.meldeplikt.meldekortservice.service.AmeldingServiceImpl
import no.nav.meldeplikt.meldekortservice.service.AmeldingServiceMock
import no.nav.sbl.dialogarena.common.cxf.CXFClient
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.apache.wss4j.dom.handler.WSHandlerConstants
import javax.security.auth.callback.CallbackHandler

object Amelding {

    fun ameldingService(environment: Environment): AmeldingService {
        return if(ConfigUtil.isCurrentlyRunningOnNais()) {
            AmeldingServiceImpl(externControlEmeldingConfig(environment))
        } else {
            AmeldingServiceMock()
        }
    }

    private fun externControlEmeldingConfig(environment: Environment): ExternControlEmeldingSOAP {
        val interceptorConfigMap = HashMap<String, Any>()
        interceptorConfigMap[WSHandlerConstants.ACTION] = WSHandlerConstants.USERNAME_TOKEN
        interceptorConfigMap[WSHandlerConstants.PASSWORD_TYPE] = "PasswordText"
        interceptorConfigMap[WSHandlerConstants.USER] = environment.personinfoUsername
        val passwordCallbackHandler = CallbackHandler { callbacks ->
            val callback = callbacks[0] as WSPasswordCallback
            callback.password = environment.personinfoPassword
        }
        interceptorConfigMap[WSHandlerConstants.PW_CALLBACK_REF] = passwordCallbackHandler

        return CXFClient(ExternControlEmeldingSOAP::class.java)
            .address(environment.emeldingUrl.toString())
            .withOutInterceptor(WSS4JOutInterceptor(interceptorConfigMap))
            .build()
    }
}