package no.nav.meldeplikt.meldekortservice.service

import no.nav.common.cxf.StsConfig
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1
import java.lang.System.getProperty

class SoapServiceImpl(
    private val oppfoelgingPing: SakOgAktivitetV1? = SoapConfig.sakOgAktivitet().configureStsForSystemUser(
        StsConfig.builder()
            .url(getProperty(SOAP_STS_URL_KEY))
            .username(getProperty(SOAP_SYSTEMUSER_USERNAME))
            .password(getProperty(SOAP_SYSTEMUSER_PASSWORD)).build()
    ).build()
) : SoapService {

    override fun pingWeblogic(): WeblogicPing {
        return try {
            oppfoelgingPing?.ping()
            WeblogicPing(true)
        } catch (e: Exception) {
            defaultLog.info("Ingen svar fra WebLogic, ping feilet", e)
            WeblogicPing(false)
        }
    }
}
