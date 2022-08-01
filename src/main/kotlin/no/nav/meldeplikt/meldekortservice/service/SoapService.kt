package no.nav.meldeplikt.meldekortservice.service

import no.nav.meldeplikt.meldekortservice.model.WeblogicPing

interface SoapService {
    fun pingWeblogic(): WeblogicPing
}
