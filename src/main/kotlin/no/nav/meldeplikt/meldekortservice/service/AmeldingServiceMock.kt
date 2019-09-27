package no.nav.meldeplikt.meldekortservice.service

import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort_kontrollert.ArsakskodeType
import no.aetat.arena.mk_meldekort.MeldekortType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortDagType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType

class AmeldingServiceMock(): AmeldingService {

    override fun kontrollerMeldekort(meldekort: MeldekortType): MeldekortKontrollertType {
        val arsakskodeType = ArsakskodeType()
        arsakskodeType.kode = "kode"
        arsakskodeType.tekst = "tekst"

        val meldekortDagType = MeldekortDagType()
        meldekortDagType.dag = 1
        meldekortDagType.meldegruppe = "DAGP"


        val meldekortKontrollertType = MeldekortKontrollertType()
        meldekortKontrollertType.arsakskoder.arsakskode.add(arsakskodeType)
        meldekortKontrollertType.meldekortDager.meldekortDag.add(meldekortDagType)
        meldekortKontrollertType.meldekortId = meldekort.hode.meldekortId.verdi
        meldekortKontrollertType.status = "OK"

        return meldekortKontrollertType
    }
}