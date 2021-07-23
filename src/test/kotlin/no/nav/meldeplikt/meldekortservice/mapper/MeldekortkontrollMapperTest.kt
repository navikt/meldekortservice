package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.MeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MeldekortkontrollMapperTest {

    private val meldekortkontrollMapper = MeldekortkontrollMapper()

    @Test
    fun testMeldekortkontrollMapper() {
        val meldekortDager = mutableListOf<MeldekortDag>()
        meldekortDager.add(MeldekortDag(1, 0F, true, true, true))
        meldekortDager.add(MeldekortDag(2, 23.5F, false, false, false))
        meldekortDager.add(MeldekortDag(3, 0F, true, true, true))
        meldekortDager.add(MeldekortDag(4, 0F, true, true, true))
        val sporsmal = Sporsmal(true, false, false, true, true, false, false, meldekortDager)
        val meldekortdetaljer = Meldekortdetaljer(
            "123",
            335,
            "99999123",
            123456,
            "202004",
            "DAGP",
            "nokkel",
            KortType.MASKINELT_OPPDATERT,
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            sporsmal,
            "Begrunnelse"
        )

        val meldekortkontroll = meldekortkontrollMapper.mapMeldekortTilMeldekortkontroll(meldekortdetaljer)

        assert(meldekortkontroll.meldekortId.equals(meldekortdetaljer.meldekortId))
        assert(meldekortkontroll.fnr.equals(meldekortdetaljer.fodselsnr))
        assert(meldekortkontroll.personId == meldekortdetaljer.personId)
        assert(meldekortkontroll.meldeperiode.fra == LocalDate.parse("2020-01-20", DateTimeFormatter.ISO_DATE))
        assert(meldekortkontroll.meldeperiode.til == LocalDate.parse("2020-02-02", DateTimeFormatter.ISO_DATE))
        assert(meldekortkontroll.meldeperiode.kortKanSendesFra == LocalDate.parse("2020-02-01", DateTimeFormatter.ISO_DATE))
        assert(meldekortkontroll.meldeperiode.periodeKode == meldekortdetaljer.meldeperiode)
        assert(meldekortkontroll.meldegruppe.equals(meldekortdetaljer.meldegruppe))
        assert(meldekortkontroll.personId.equals(meldekortdetaljer.personId))
        assert(meldekortkontroll.sporsmal.annetFravaer == sporsmal.annetFravaer)
        assert(meldekortkontroll.sporsmal.arbeidet == sporsmal.arbeidet)
        assert(meldekortkontroll.sporsmal.arbeidssoker == sporsmal.arbeidssoker)
        assert(meldekortkontroll.sporsmal.forskudd == sporsmal.forskudd)
        assert(meldekortkontroll.sporsmal.kurs == sporsmal.kurs)
        assert(meldekortkontroll.sporsmal.signatur == sporsmal.signatur)
        assert(meldekortkontroll.sporsmal.syk == sporsmal.syk)
        assert(meldekortkontroll.fravaersdager.size == meldekortDager.size)
        sammenlignMeldekortDag(
            meldekortkontroll.fravaersdager[0],
            meldekortDager[0],
            meldekortkontroll.meldeperiode.fra!!
        )
        assert(meldekortkontroll.fravaersdager[0].harSyk!!)
        sammenlignMeldekortDag(
            meldekortkontroll.fravaersdager[1],
            meldekortDager[1],
            meldekortkontroll.meldeperiode.fra!!
        )
        assert(meldekortkontroll.fravaersdager[1].arbeidTimer!! > 0)
        assert(meldekortkontroll.begrunnelse == meldekortdetaljer.begrunnelse)
    }

    private fun sammenlignMeldekortDag(fra: FravaerInn, til: MeldekortDag, foersteDag: LocalDate) {
        assert(fra.arbeidTimer == til.arbeidetTimerSum!!.toDouble())
        assert(fra.dag == foersteDag.plusDays(til.dag.toLong() - 1))
    }
}
