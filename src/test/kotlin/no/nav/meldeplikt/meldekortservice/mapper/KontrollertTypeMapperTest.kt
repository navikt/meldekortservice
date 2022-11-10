package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollFeil
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import org.junit.jupiter.api.Test

class KontrollertTypeMapperTest {

    private val kontrollertTypeMapper = KontrollertTypeMapper()

    @Test
    fun tmapKontrollResponseToKontrollertTypeSkalReturnereOk() {
        val feilListe = mutableListOf<KontrollFeil>()

        val fra = KontrollResponse(87576,  "OK", feilListe)
        val til: MeldekortKontrollertType = kontrollertTypeMapper.mapKontrollResponseToKontrollertType(fra)

        assert(til.meldekortId == fra.meldekortId)
        assert(til.status == "OK")
        assert(til.meldekortDager.meldekortDag.size == fra.feilListe.size)
        assert(til.arsakskoder.arsakskode.size == fra.feilListe.size)
    }

    @Test
    fun mapKontrollResponseToKontrollertTypeSkalReturnereFeil() {
        val feilListe = mutableListOf<KontrollFeil>()
        val feil1 = KontrollFeil("X05", "Set sail for fail", 1)
        val feil2 = KontrollFeil("S09", "On the failboat", 3, arrayOf("param 1", "param 2"))
        feilListe.add(feil1)
        feilListe.add(feil2)

        val fra = KontrollResponse(87576,  "OK", feilListe)
        val til: MeldekortKontrollertType = kontrollertTypeMapper.mapKontrollResponseToKontrollertType(fra)

        assert(til.meldekortId == fra.meldekortId)
        assert(til.status == "FEIL")
        assert(til.meldekortDager.meldekortDag.size == fra.feilListe.size)
        assert(til.meldekortDager.meldekortDag[0].dag == feil1.dag)
        assert(til.meldekortDager.meldekortDag[1].dag == feil2.dag)
        assert(til.arsakskoder.arsakskode.size == fra.feilListe.size)
        assert(til.arsakskoder.arsakskode[0].kode == feil1.kode)
        assert(til.arsakskoder.arsakskode[0].tekst == feil1.tekst)
        assert(til.arsakskoder.arsakskode[0].params == null)
        assert(til.arsakskoder.arsakskode[1].kode == feil2.kode)
        assert(til.arsakskoder.arsakskode[1].tekst == feil2.tekst)
        assert(til.arsakskoder.arsakskode[1].params.contentEquals(feil2.params))
    }
}
