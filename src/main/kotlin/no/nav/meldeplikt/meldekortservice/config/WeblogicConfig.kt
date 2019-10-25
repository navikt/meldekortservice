package no.nav.meldeplikt.meldekortservice.config

import no.nav.sbl.dialogarena.common.cxf.CXFClient
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1
import org.apache.cxf.annotations.SchemaValidation

object WeblogicConfig {

    private val environment = Environment()

    @SchemaValidation
    fun arbeidOgAktivitetSakV1(): SakOgAktivitetV1{
        return CXFClient(SakOgAktivitetV1::class.java)
            .address(environment.sakOgAktivitetUri)
            .configureStsForSystemUser()
            .build()
    }
}