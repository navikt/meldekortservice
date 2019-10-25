package no.nav.meldeplikt.meldekortservice.config

import no.nav.sbl.dialogarena.common.cxf.CXFClient
import no.nav.tjeneste.virksomhet.arbeidogaktivitetsak.v1.ArbeidOgAktivitetSakV1

object WeblogicConfig {

    private val environment = Environment()

    fun arbeidOgAktivitetSakV1(): ArbeidOgAktivitetSakV1 {
        return CXFClient(ArbeidOgAktivitetSakV1::class.java)
            .address(environment.arbeidOgAktivitetUri)
            .configureStsForSystemUser()
            .build()
    }


}