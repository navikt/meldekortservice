package no.nav.meldeplikt.meldekortservice.service

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.api.jsonMapper
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortTypeMapper
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.getLogger
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1

class SoapServiceImpl(
        private val amelding: ExternControlEmeldingSOAP,
        private val oppfoelgingPing: SakOgAktivitetV1? = SoapConfig.sakOgAktivitet().configureStsForSystemUser().build()
) : SoapService {

    private val log = getLogger(SoapServiceImpl::class)


    override fun kontrollerMeldekort(meldekortdetaljer: Meldekortdetaljer): MeldekortKontrollertType {
        val meldekort = MeldekortTypeMapper.mapMeldekortType(meldekortdetaljer)
        defaultLog.info(
            "Mappet meldekort er: " + jsonMapper.writeValueAsString(meldekort)
        )
        return amelding.kontrollerEmeldingMeldekort(meldekort)
    }

    override fun pingWeblogic(): WeblogicPing {
        return try {
            oppfoelgingPing?.ping()
            WeblogicPing(true)
        } catch (e: Exception) {
            log.info("Ingen svar fra WebLogic, ping feilet", e)
            WeblogicPing(false)
        }
    }
}