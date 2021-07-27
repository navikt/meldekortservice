package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Svar
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.BooleanVerdi
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.LongVerdi
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.verdi.StringVerdi
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Hode as ArenaHode
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Meldekort as ArenaMeldekort
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.MeldekortDag as ArenaMeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Spm as ArenaSpm

class MeldekortdetaljerMapperTest {

    @Test
    fun testMeldekortdetaljerMapper() {
        val hode = ArenaHode(
            LongVerdi(123L),
            StringVerdi("99999123"),
            LongVerdi(1911L),
            "202009",
            "DAGP",
            "NOK",
            KortType.ELEKTRONISK.code,
            LocalDate.parse("2020-01-20", DateTimeFormatter.ISO_DATE),
            LocalDate.parse("2020-01-21", DateTimeFormatter.ISO_DATE),
            "Kommentar"
        )
        val dag1 = ArenaMeldekortDag(1, StringVerdi("20"), BooleanVerdi(false), BooleanVerdi(false), BooleanVerdi(false))
        val dag2 = ArenaMeldekortDag(2, StringVerdi("0"), BooleanVerdi(false), BooleanVerdi(false), BooleanVerdi(false))
        val svarJa = Svar(BooleanVerdi(true), BooleanVerdi(false))
        val svarNei = Svar(BooleanVerdi(false), BooleanVerdi(true))
        val spm = ArenaSpm(
            svarJa,
            svarJa,
            svarNei,
            svarNei,
            svarNei,
            BooleanVerdi(false),
            listOf(dag1, dag2),
            BooleanVerdi(false)
        )


        val fra = ArenaMeldekort(hode, spm)
        val til: Meldekortdetaljer = MeldekortdetaljerMapper.mapOrdsMeldekortTilMeldekortdetaljer(fra)

        assert(til.id == "1")
        assert(til.personId == fra.hode.personId.verdi)
        assert(til.fodselsnr == fra.hode.fodselsnr.verdi)
        assert(til.meldekortId == fra.hode.meldekortId.verdi)
        assert(til.meldeperiode == fra.hode.meldeperiode)
        assert(til.arkivnokkel == fra.hode.arkivnokkel)
        assert(til.kortType == KortType.getByCode(fra.hode.kortType))
        assert(til.meldeDato == fra.hode.meldeDato)
        assert(til.lestDato == fra.hode.lestDato)
        assert(til.begrunnelse == fra.hode.kommentar)
        assert(til.sporsmal?.arbeidet == fra.spm.arbeidet.svarJa.verdi)
        assert(til.sporsmal?.arbeidssoker == fra.spm.arbeidssoker.svarJa.verdi)
        assert(til.sporsmal?.syk == fra.spm.syk.svarJa.verdi)
        assert(til.sporsmal?.annetFravaer == fra.spm.annetFravaer.svarJa.verdi)
        assert(til.sporsmal?.kurs == fra.spm.kurs.svarJa.verdi)

        assert(til.sporsmal?.forskudd == fra.spm.forskudd.verdi)
        assert(til.sporsmal?.signatur == fra.spm.signatur.verdi)

        // TODO: Dager
    }
}
