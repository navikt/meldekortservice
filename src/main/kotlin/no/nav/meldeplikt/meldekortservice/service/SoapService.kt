package no.nav.meldeplikt.meldekortservice.service

import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer

interface SoapService {
    fun kontrollerMeldekort(meldekortdetaljer: Meldekortdetaljer): MeldekortKontrollertType
    fun pingWeblogic(): WeblogicPing
}