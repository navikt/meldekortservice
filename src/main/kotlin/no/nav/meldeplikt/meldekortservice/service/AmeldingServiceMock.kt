package no.nav.meldeplikt.meldekortservice.service

import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer

class AmeldingServiceMock(): AmeldingService {

    override fun kontrollerMeldekort(meldekortdetaljer: Meldekortdetaljer): MeldekortKontrollertType {
        val meldekortKontrollertType = MeldekortKontrollertType()
        meldekortKontrollertType.meldekortId = meldekortdetaljer.meldekortId
        meldekortKontrollertType.status = "OK"

        return meldekortKontrollertType
    }
}