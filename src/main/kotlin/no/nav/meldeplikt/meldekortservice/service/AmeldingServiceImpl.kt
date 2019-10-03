package no.nav.meldeplikt.meldekortservice.service

import mu.KLogging
import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort.MeldekortType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortTypeMapper
import no.nav.meldeplikt.meldekortservice.model.Meldekortdetaljer

class AmeldingServiceImpl(externControlEmeldingSOAP: ExternControlEmeldingSOAP) : AmeldingService {

    companion object: KLogging()

    private val amelding = externControlEmeldingSOAP

    override fun kontrollerMeldekort(meldekortdetaljer: Meldekortdetaljer): MeldekortKontrollertType {
        val meldekort = MeldekortTypeMapper.mapMeldekortType(meldekortdetaljer)
        val env = Environment()
        logger.info { "Environment variabler. env.emeldingUrl: ${env.emeldingUrl} environment.personinfoUsername: ${env.personinfoUsername}"}
        return try {
            val meldekortKontrollertTypeSoap = amelding.kontrollerEmeldingMeldekort(meldekort)
            logger.info{ "Valideringstatus: ${meldekortKontrollertTypeSoap.status}" }
            println("Valideringstatus: ${meldekortKontrollertTypeSoap.status}")

            meldekortKontrollertTypeSoap
        } catch (e: Exception) {
            logger.error(e) { "Innsending av meldekort feilet. Meldekortdetaljer: $meldekortdetaljer" }
            val kontrollertType = MeldekortKontrollertType()
            kontrollertType.status = "Error"
            kontrollertType
        }
    }
}