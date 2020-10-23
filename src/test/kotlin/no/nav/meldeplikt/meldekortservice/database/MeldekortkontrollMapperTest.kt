package no.nav.meldeplikt.meldekortservice.database

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortkontrollMapper
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.MeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.MeldeperiodeInn
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assert

class MeldekortkontrollMapperTest {

    private val meldekortkontrollMapper = MeldekortkontrollMapper()

    @Test
    fun testMeldekortkontrollMapper() {
        var meldekortDager = mutableListOf<MeldekortDag>()
        meldekortDager.add(MeldekortDag(1, 0F, true, true, true, "DAGP"))
        meldekortDager.add(MeldekortDag(2, 23.5F, false, false, false, "ATTF"))
        meldekortDager.add(MeldekortDag(3, 0F, true, true, true, "ATTF"))
        meldekortDager.add(MeldekortDag(4, 0F, true, true, true, "ATTF"))
        var sporsmal = Sporsmal(true, false, false, true, true, false, false, meldekortDager)
        var meldekortdetaljer = Meldekortdetaljer("123", 335, "99999123", 123456, "202004",
            "nokkel", KortType.MASKINELT_OPPDATERT, LocalDate.now(), LocalDate.now().plusDays(1), sporsmal, "Begrunnelse")

        var meldekortkontroll = meldekortkontrollMapper.mapMeldekortTilMeldekortkontroll(meldekortdetaljer)

        assert(meldekortkontroll.meldekortId.equals(meldekortdetaljer.meldekortId)) // id eller meldekortid???
        assert(meldekortkontroll.meldeperiode.fra == LocalDate.parse("2020-01-20", DateTimeFormatter.ISO_DATE))
        assert(meldekortkontroll.meldeperiode.til == LocalDate.parse("2020-02-02", DateTimeFormatter.ISO_DATE))
        assert(meldekortkontroll.meldeperiode.kortKanSendesFra == LocalDate.parse("2020-02-01", DateTimeFormatter.ISO_DATE))
        assert(meldekortkontroll.meldeperiode.periodeKode == meldekortdetaljer.meldeperiode)
        assert(meldekortkontroll.meldegruppe.equals("DAGP"))
        assert(meldekortkontroll.sporsmal.annetFravaer == sporsmal.annetFravaer)
        assert(meldekortkontroll.sporsmal.arbeidet == sporsmal.arbeidet)
        assert(meldekortkontroll.sporsmal.arbeidssoker == sporsmal.arbeidssoker)
        assert(meldekortkontroll.sporsmal.forskudd == sporsmal.forskudd)
        assert(meldekortkontroll.sporsmal.kurs == sporsmal.kurs)
        assert(meldekortkontroll.sporsmal.signatur == sporsmal.signatur)
        assert(meldekortkontroll.sporsmal.syk == sporsmal.syk)
        assert(meldekortkontroll.fravaersdager.size == meldekortDager.size)
        sammenlignMeldekortDag(meldekortkontroll.fravaersdager[0], meldekortDager[0], meldekortkontroll.meldeperiode.fra!!)
        assert(meldekortkontroll.fravaersdager[0].type.equals("SYKDOM"))
        sammenlignMeldekortDag(meldekortkontroll.fravaersdager[1], meldekortDager[1], meldekortkontroll.meldeperiode.fra!!)
        assert(meldekortkontroll.fravaersdager[1].type.equals("ARBEIDS_FRAVAER"))
        assert(meldekortkontroll.begrunnelse == meldekortdetaljer.begrunnelse)
    }

    private fun sammenlignMeldekortDag(fra: FravaerInn, til: MeldekortDag, foersteDag: LocalDate) {
        assert(fra.arbeidTimer == til.arbeidetTimerSum!!.toDouble())
        assert(fra.dag == foersteDag.plusDays(til.dag.toLong()))
    }
}