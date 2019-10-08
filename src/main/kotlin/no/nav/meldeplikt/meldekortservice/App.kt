package no.nav.meldeplikt.meldekortservice

import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.Server
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais

fun main() {
    val environment = Environment()

    Server.configure(environment).start()

    if (!isCurrentlyRunningOnNais()) {
        println("************** MOCK / TEST **************")
    }
}