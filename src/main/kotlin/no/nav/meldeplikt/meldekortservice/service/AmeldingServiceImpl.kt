package no.nav.meldeplikt.meldekortservice.service

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort.MeldekortType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortTypeMapper
import no.nav.meldeplikt.meldekortservice.model.Meldekortdetaljer

class AmeldingServiceImpl(externControlEmeldingSOAP: ExternControlEmeldingSOAP) : AmeldingService {

    private val amelding = externControlEmeldingSOAP

    override fun kontrollerMeldekort(meldekortdetaljer: Meldekortdetaljer): MeldekortKontrollertType {
        val meldekort = MeldekortTypeMapper.mapMeldekortType(meldekortdetaljer)
        val meldekortKontrollertTypeSoap = amelding.kontrollerEmeldingMeldekort(meldekort)

        if (meldekortKontrollertTypeSoap.status == "OK") {
            // TODO Sett meldekort som innsendt
        }
        return meldekortKontrollertTypeSoap
    }
}