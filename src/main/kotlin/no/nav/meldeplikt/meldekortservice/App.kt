package no.nav.meldeplikt.meldekortservice

import no.nav.meldeplikt.meldekortservice.utils.ConfigUtil
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.Server

fun main() {
    val environment = Environment()

    Server.configure(environment).start()

    if (!ConfigUtil.isCurrentlyRunningOnNais()) {
        println("************** MOCK / TEST **************")
    }
}