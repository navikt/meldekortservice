package no.nav.meldeplikt.meldekortservice.service

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort_kontrollert.ArsakskodeType
import no.aetat.arena.mk_meldekort.MeldekortType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType

class AmeldingServiceMock(): AmeldingService {

    override fun kontrollerMeldekort(meldekort: MeldekortType): MeldekortKontrollertType {
        val aarsakskodeType = ArsakskodeType()
        aarsakskodeType.kode = "kode"
        aarsakskodeType.tekst = "tekst"
        val kode = MeldekortKontrollertType.Arsakskoder().arsakskode.add(aarsakskodeType)
        val meldekortKontrollertType = MeldekortKontrollertType()
        meldekortKontrollertType.arsakskoder = kode
    }
}