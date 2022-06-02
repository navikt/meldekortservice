package no.nav.meldeplikt.meldekortservice.service

import no.nav.meldeplikt.meldekortservice.model.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer

interface SoapService {
    fun pingWeblogic(): WeblogicPing
}
