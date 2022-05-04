package no.nav.meldeplikt.meldekortservice.service

import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1

class SoapServiceImpl(
    private val oppfoelgingPing: SakOgAktivitetV1? = SoapConfig.sakOgAktivitet().configureStsForSystemUser().build()
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
