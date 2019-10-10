package no.nav.meldeplikt.meldekortservice.service

import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.Meldekortdetaljer

interface AmeldingService {
    fun kontrollerMeldekort(meldekortdetaljer: Meldekortdetaljer): MeldekortKontrollertType
}