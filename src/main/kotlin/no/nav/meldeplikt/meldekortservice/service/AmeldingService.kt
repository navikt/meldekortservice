package no.nav.meldeplikt.meldekortservice.service

import no.aetat.arena.mk_meldekort.MeldekortType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType

interface AmeldingService {
    fun kontrollerMeldekort(meldekort: MeldekortType): MeldekortKontrollertType
}