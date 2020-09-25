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
            kortType = meldekort.kortType.toString(),
            kortStatus = "xxx",
            meldegruppe = trekkutMeldegruppe(meldekort),
            meldeperiode = trekkutMeldeperiode(meldekort),
            fravaersdager = trekkutFravaersdager(meldekort),
            sporsmal = trekkutSporsmal(meldekort)
        )
    }

    private fun trekkutMeldeperiode(meldekort: Meldekortdetaljer): MeldeperiodeInn {
        val dagerFoer = 1L // TODO: Hent fra parameter
        val fraD: LocalDate = LocalDate.parse(meldekort.meldeperiode.substring(0, 3)+"-W"+meldekort.meldeperiode.substring(4, 5)+"-1", ISO_WEEK_DATE)
        return MeldeperiodeInn(
            fra = fraD,
            til = fraD.plusDays(13L),
            kortKanSendesFra = fraD.plusDays(13L-dagerFoer),
            kanKortSendes = LocalDate.now() >= (fraD.plusDays(13L-dagerFoer)),
            periodeKode = meldekort.meldeperiode
        )
    }

    private fun trekkutFravaersdager(meldekort: Meldekortdetaljer): List<FravaerInn> {
        var fravaer: MutableList<FravaerInn> = emptyList<FravaerInn>() as MutableList<FravaerInn>
        val fraD: LocalDate = LocalDate.parse(meldekort.meldeperiode.substring(0, 3)+"-W"+meldekort.meldeperiode.substring(4, 5)+"-1", ISO_WEEK_DATE)
        for (mdag in meldekort.sporsmal?.meldekortDager!!) {
            var mtype = "A"
            if (mdag.annetFravaer!!) mtype = "X"
            if (mdag.kurs!!) mtype = "K"
            if (mdag.syk!!) mtype = "S"

            fravaer.add(FravaerInn(
                dag = fraD.plusDays(mdag.dag.toLong()),
                type = mtype,
                arbeidTimer = mdag.arbeidetTimerSum?.toDouble()
            ))
        }
        return fravaer
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

//    "MELDEGRUPPEKODE"             "MELDEGRUPPENAVN"             "NIVAA"                       "STATUS_VERDILISTE"
//    "ARBS"                        "Ingen ytelser"               "0"                           "J"
//    "FY"                          "Flere meldegrupper"          "0"                           "N"
//    "INDIV"                       "Individstønad"               "1"                           "J"
//    "ATTF"                        "Arbeidsavklaringspenger"     "2"                           "J"
//    "DAGP"                        "Dagpenger"                   "3"                           "J"

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
