package no.nav.meldeplikt.meldekortservice.service

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortTypeMapper
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.utils.getLogger

class SoapServiceImpl(externControlEmeldingSOAP: ExternControlEmeldingSOAP) : SoapService {

    private val log = getLogger(SoapServiceImpl::class)

    private val amelding = externControlEmeldingSOAP

    override fun kontrollerMeldekort(meldekortdetaljer: Meldekortdetaljer): MeldekortKontrollertType {
        val meldekort = MeldekortTypeMapper.mapMeldekortType(meldekortdetaljer)
        return amelding.kontrollerEmeldingMeldekort(meldekort)
    }

    override fun pingWeblogic(): WeblogicPing {
        val oppfoelgingPing = SoapConfig.sakOgAktivitet()
            .configureStsForSystemUser()
            .build()

        return try {
            oppfoelgingPing.ping()
            WeblogicPing(true)
        } catch (e: Exception) {
            log.info("Ingen svar fra WebLogic, ping feilet", e)
            WeblogicPing(false)
        }
    }
}