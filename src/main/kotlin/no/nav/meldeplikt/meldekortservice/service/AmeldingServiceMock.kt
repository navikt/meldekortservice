package no.nav.meldeplikt.meldekortservice.service

import no.aetat.arena.mk_meldekort.MeldekortType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType

class AmeldingServiceMock(): AmeldingService {

    override fun kontrollerMeldekort(meldekort: MeldekortType): MeldekortKontrollertType {
        val meldekortKontrollertType = MeldekortKontrollertType()
        meldekortKontrollertType.meldekortId = meldekort.hode.meldekortId.verdi
        meldekortKontrollertType.status = "OK"

        return meldekortKontrollertType
    }
}