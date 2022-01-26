package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_WEEK_DATE

class MeldekortkontrollMapper {

    val KILDE_MELDEPLIKT = "MELDEPLIKT"

    fun mapMeldekortTilMeldekortkontroll(meldekort: Meldekortdetaljer): Meldekortkontroll {
        val fraD: LocalDate = finnMeldeperiodeFraDato(meldekort.meldeperiode)

        return Meldekortkontroll(
            meldekortId = meldekort.meldekortId,
            fnr = meldekort.fodselsnr,
            personId = meldekort.personId,
            kilde = KILDE_MELDEPLIKT,
            kortType = meldekort.kortType.name,
            meldedato = meldekort.meldeDato,
            periodeFra = fraD,
            periodeTil = fraD.plusDays(13L),
            meldegruppe = meldekort.meldegruppe,
            annetFravaer = meldekort.sporsmal?.annetFravaer,
            arbeidet = meldekort.sporsmal?.arbeidet,
            arbeidssoker = meldekort.sporsmal?.arbeidssoker,
            kurs = meldekort.sporsmal?.kurs,
            syk = meldekort.sporsmal?.syk,
            begrunnelse = meldekort.begrunnelse,
            meldekortdager = trekkutFravaersdager(meldekort)
        )
    }

    // Her utleder vi spørsmålene harAnnet, harKurs og harSyk fra avkrysningene. Dette burde være avkrysningene
    // som blir gjort i frontenden, men de blir ikke sendt fra frontenden.
    // Vi gjør dette fordi meldekortkontroll-api konsistenssjekker kryssene mot spørsmålene, noe som kan være
    // aktuelt når meldekort kommer fra andre kilder enn vår frontend
    private fun trekkutFravaersdager(meldekort: Meldekortdetaljer): List<FravaerInn> {
        val fravaer = mutableListOf<FravaerInn>()
        val fraD: LocalDate = finnMeldeperiodeFraDato(meldekort.meldeperiode)
        for (mdag in meldekort.sporsmal?.meldekortDager!!) {

            fravaer.add(
                FravaerInn(
                    // Vi har -1 her fordi datoene som kommer inn fra frontend er feil.
                    dag = fraD.plusDays(mdag.dag.toLong() - 1),
                    harAnnet = mdag.annetFravaer == true,
                    harKurs = mdag.kurs == true,
                    harSyk = mdag.syk == true,
                    arbeidTimer = mdag.arbeidetTimerSum?.toDouble()
                )
            )
        }
        return fravaer
    }

    // Finn første dag i meldeperioden
    private fun finnMeldeperiodeFraDato(meldeperiode: String): LocalDate {
        return LocalDate.parse(meldeperiode.substring(0, 4) + "-W" + meldeperiode.substring(4, 6) + "-1", ISO_WEEK_DATE)
    }

}
