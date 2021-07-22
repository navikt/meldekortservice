package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.mapper.MeldekortTypeMapper.mapMeldekortType
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.MeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.datatype.DatatypeFactory

class MeldekortTypeMappertest {

    @Test
    fun testMeldekortTypeMapper() {
        var meldekortDager = mutableListOf<MeldekortDag>()
        meldekortDager.add(MeldekortDag(1, 0F, true, true, true))
        meldekortDager.add(MeldekortDag(2, 23.5F, false, false, false))
        meldekortDager.add(MeldekortDag(3, 0F, true, true, true))
        meldekortDager.add(MeldekortDag(4, 0F, true, true, true))

        val sporsmal = Sporsmal(true, true, false, false, false, false, false, meldekortDager)

        val fra = Meldekortdetaljer(
            "33",
            150L,
            "999444",
            7655687,
            "202013",
            "Skelly",
            "arkiv",
            KortType.ORDINAER,
            LocalDate.parse("2020-01-20", DateTimeFormatter.ISO_DATE),
            LocalDate.parse("2020-01-21", DateTimeFormatter.ISO_DATE),
            sporsmal,
            "Grunngitt"
        )

        val til = mapMeldekortType(fra)

        assert(til.id == fra.id)

        assert(til.hode.personId.verdi == fra.personId)
        assert(til.hode.fodselsnr.verdi == fra.fodselsnr)
        assert(til.hode.meldekortId.verdi == fra.meldekortId)
        assert(til.hode.meldeperiode == fra.meldeperiode)
        assert(til.hode.arkivnokkel == fra.arkivnokkel)
        assert(til.hode.kortType == fra.kortType.code)
        assert(til.hode.meldeDato == DatatypeFactory.newInstance().newXMLGregorianCalendar(fra.meldeDato.toString()))
        assert(til.hode.lestDato == DatatypeFactory.newInstance().newXMLGregorianCalendar(fra.lestDato.toString()))
        assert(til.hode.kommentar == fra.begrunnelse)

        assert(til.spm.arbeidssoker.svarJa.isVerdi == fra.sporsmal?.arbeidssoker)
        assert(til.spm.arbeidet.svarJa.isVerdi == fra.sporsmal?.arbeidet)
        assert(til.spm.syk.svarJa.isVerdi == fra.sporsmal?.syk)
        assert(til.spm.annetFravaer.svarJa.isVerdi == fra.sporsmal?.annetFravaer)
        assert(til.spm.kurs.svarJa.isVerdi == fra.sporsmal?.kurs)
//        assert(til.spm.signatur.isVerdi==fra.sporsmal?.signatur)
//        assert(til.spm.forskudd.isVerdi==fra.sporsmal?.forskudd)

        assert(til.spm.meldekortDager.meldekortDag[0].dag == meldekortDager[0].dag)
        assert(til.spm.meldekortDager.meldekortDag[1].arbeidetTimerSum.verdi == meldekortDager[1].arbeidetTimerSum)
        assert(til.spm.meldekortDager.meldekortDag[2].syk.isVerdi == meldekortDager[2].syk)
        assert(til.spm.meldekortDager.meldekortDag[3].annetFravaer.isVerdi == meldekortDager[3].annetFravaer)
        assert(til.spm.meldekortDager.meldekortDag[0].kurs.isVerdi == meldekortDager[0].kurs)
//        assert(til.spm.meldekortDager.meldekortDag[1].meldegruppe==meldekortDager[1].meldegruppe)

    }
}