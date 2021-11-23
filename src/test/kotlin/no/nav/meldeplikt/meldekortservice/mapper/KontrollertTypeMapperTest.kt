package no.nav.meldeplikt.meldekortservice.mapper

import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollFeil
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import org.junit.jupiter.api.Test

class KontrollertTypeMapperTest {

    private val kontrollertTypeMapper = KontrollertTypeMapper()

    @Test
    fun testMapKontrollResponseToKontrollertType() {
        var feilListe = mutableListOf<KontrollFeil>()
        val feil1 = KontrollFeil("X05", "Set sail for fail", 1)
        val feil2 = KontrollFeil("S09", "On the failboat", 3)
        feilListe.add(feil1)
        feilListe.add(feil2)

        var fra = KontrollResponse(87576, "ignore me", "OK", feilListe)
        var til: MeldekortKontrollertType = kontrollertTypeMapper.mapKontrollResponseToKontrollertType(fra)

        assert(til.arsakskoder.arsakskode.size == fra.feilListe.size)
        assert(til.meldekortId == fra.meldekortId)
        assert(til.status == "FEIL")
        assert(til.meldekortDager.meldekortDag.size == fra.feilListe.size)
        assert(til.arsakskoder.arsakskode[0].kode == feil1.kode)
        assert(til.meldekortDager.meldekortDag[1].dag == feil2.dag)
    }
}
