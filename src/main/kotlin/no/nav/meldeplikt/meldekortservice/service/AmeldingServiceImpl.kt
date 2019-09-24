package no.nav.meldeplikt.meldekortservice.service

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort.MeldekortType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType

class AmeldingServiceImpl : AmeldingService {

    lateinit var externControlEmeldingSOAP: ExternControlEmeldingSOAP

    override fun kontrollerMeldekort(meldekort: MeldekortType): MeldekortKontrollertType {
        val meldekortKontrollertTypeSoap = externControlEmeldingSOAP.kontrollerEmeldingMeldekort(meldekort)

        if (meldekortKontrollertTypeSoap.status == "OK") {
            // TODO Sett meldekort som innsendt
        }
        return meldekortKontrollertTypeSoap
    }
}