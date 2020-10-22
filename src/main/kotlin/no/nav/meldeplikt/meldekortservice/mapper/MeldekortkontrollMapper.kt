package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.MeldeperiodeInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Sporsmal
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_WEEK_DATE

class MeldekortkontrollMapper {

    fun mapMeldekortTilMeldekortkontroll(meldekort: Meldekortdetaljer): Meldekortkontroll {
        return Meldekortkontroll(
            meldekortId = meldekort.meldekortId,
            kortType = meldekort.kortType.name,
            kortStatus = "SENDT", // TODO: Finn ut hvordan vi forholder oss til denne. Ligger ikke i request.
            meldegruppe = trekkutMeldegruppe(meldekort),
            meldeperiode = trekkutMeldeperiode(meldekort),
            fravaersdager = trekkutFravaersdager(meldekort),
            sporsmal = trekkutSporsmal(meldekort),
            begrunnelse = meldekort.begrunnelse
        )
    }

    private fun trekkutMeldeperiode(meldekort: Meldekortdetaljer): MeldeperiodeInn {
        val dagerFoer = 1L // TODO: Hent fra parameter i database
        val fraD: LocalDate = ukeTilDato(meldekort.meldeperiode)
        return MeldeperiodeInn(
            fra = fraD,
            til = fraD.plusDays(13L),
            kortKanSendesFra = fraD.plusDays(13L-dagerFoer),
            kanKortSendes = LocalDate.now() >= (fraD.plusDays(13L-dagerFoer)),
            periodeKode = meldekort.meldeperiode
        )
    }

    private fun trekkutFravaersdager(meldekort: Meldekortdetaljer): List<FravaerInn> {
        var fravaer = mutableListOf<FravaerInn>()
        val fraD: LocalDate = ukeTilDato(meldekort.meldeperiode)
        for (mdag in meldekort.sporsmal?.meldekortDager!!) {
            var mtype = "ARBEIDS_FRAVAER"
            if (mdag.annetFravaer!!) mtype = "ANNET_FRAVAER"
            if (mdag.kurs!!) mtype = "KURS_UTDANNING"
            if (mdag.syk!!) mtype = "SYKDOM"

            fravaer.add(FravaerInn(
                dag = fraD.plusDays(mdag.dag.toLong()),
                type = mtype,
                arbeidTimer = mdag.arbeidetTimerSum?.toDouble()
            ))
        }
        return fravaer
    }

    private fun ukeTilDato(meldeperiode: String): LocalDate {
        return LocalDate.parse(meldeperiode.substring(0, 4)+"-W"+meldeperiode.substring(4, 6)+"-1", ISO_WEEK_DATE)
    }

    private fun trekkutSporsmal(meldekort: Meldekortdetaljer): Sporsmal {
        return Sporsmal(
            arbeidssoker = meldekort.sporsmal?.arbeidssoker,
            arbeidet = meldekort.sporsmal?.arbeidet,
            syk = meldekort.sporsmal?.syk,
            annetFravaer = meldekort.sporsmal?.annetFravaer,
            kurs = meldekort.sporsmal?.kurs,
            forskudd = meldekort.sporsmal?.forskudd,
            signatur = meldekort.sporsmal?.signatur
        )
    }

    private fun trekkutMeldegruppe(meldekort: Meldekortdetaljer): String {
        var nivaa = 0;
        val meldegrupper: Array<String> = arrayOf("ARBS", "INDIV", "ATTF", "DAGP")
        meldekort.sporsmal?.meldekortDager?.forEach {
            for (i in 1..3) {
                if (it.meldegruppe == meldegrupper[i] && nivaa < i) nivaa = i
            }
        }
        return meldegrupper[nivaa]
    }

}
