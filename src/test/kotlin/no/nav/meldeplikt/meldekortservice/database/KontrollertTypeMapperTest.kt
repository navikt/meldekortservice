package no.nav.meldeplikt.meldekortservice.database

import com.fasterxml.jackson.databind.json.JsonMapper
import no.aetat.arena.mk_meldekort_kontrollert.ArsakskodeType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.mapper.KontrollertTypeMapper
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortkontrollMapper
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.MeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollFeil
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assert

class KontrollertTypeMapperTest {

private val kontrollertTypeMapper = KontrollertTypeMapper()

    @Test
    fun testMapKontrollResponseToKontrollertType() {
        var feilListe = mutableListOf<KontrollFeil>()
        val feil1 = KontrollFeil("X05", "Set sail for fail", 1)
        val feil2 = KontrollFeil("S09", "On the failboat", 3)
        feilListe.add(feil1)
        feilListe.add(feil2)

        var fra = KontrollResponse(87576, "ignore me", feilListe)
        var til: MeldekortKontrollertType = kontrollertTypeMapper.mapKontrollResponseToKontrollertType(fra)

        assert(til.arsakskoder.arsakskode.size == fra.feilListe.size)
        assert(til.meldekortId == fra.meldekortId)
        assert(til.status == "FEIL")
        assert(til.meldekortDager.meldekortDag.size == fra.feilListe.size)
        assert(til.arsakskoder.arsakskode[0].kode == feil1.kode)
        assert(til.meldekortDager.meldekortDag[1].dag == feil2.dag)
    }
}
