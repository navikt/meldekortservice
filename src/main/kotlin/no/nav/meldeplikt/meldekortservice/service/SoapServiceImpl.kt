package no.nav.meldeplikt.meldekortservice.service

import mu.KLogging
import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortTypeMapper
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer

class SoapServiceImpl(externControlEmeldingSOAP: ExternControlEmeldingSOAP) : SoapService {

    companion object: KLogging()

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
            println("Ping vellykket")
            WeblogicPing(true)
        } catch (e: Exception) {
            println(e)
            WeblogicPing(false)
        }
    }
}