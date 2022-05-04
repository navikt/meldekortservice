package no.nav.meldeplikt.meldekortservice.service

import no.nav.meldeplikt.meldekortservice.model.WeblogicPing

class SoapServiceMock() : SoapService {

    override fun pingWeblogic(): WeblogicPing {
        return WeblogicPing(true)
    }
}
