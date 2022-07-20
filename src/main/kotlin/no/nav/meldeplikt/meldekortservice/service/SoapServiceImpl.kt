package no.nav.meldeplikt.meldekortservice.service

import no.nav.common.cxf.StsConfig
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1

class SoapServiceImpl(
    private val oppfoelgingPing: SakOgAktivitetV1? = SoapConfig.sakOgAktivitet().configureStsForSystemUser(
        StsConfig.builder()
            .url(System.getProperty(STS_URL_KEY) + STS_PATH)
            .username(System.getProperty(SYSTEMUSER_USERNAME))
            .password(System.getProperty(SYSTEMUSER_PASSWORD)).build()
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
