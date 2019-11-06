package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.MeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.Meldekort as OrdsMeldekort
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.Spm as OrdsSpm
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.ords.MeldekortDag as OrdsMeldekortDag

object MeldekortdetaljerMapper {

    fun mapOrdsMeldekortTilMeldekortdetaljer(meldekort: OrdsMeldekort): Meldekortdetaljer {
        return Meldekortdetaljer(
            id = "1",
            personId = meldekort.hode.personId.verdi,
            fodselsnr = meldekort.hode.fodselsnr.verdi,
            meldekortId = meldekort.hode.meldekortId.verdi,
            meldeperiode = meldekort.hode.meldeperiode,
            arkivnokkel = meldekort.hode.arkivnokkel,
            kortType = KortType.getByCode(meldekort.hode.kortType),
            meldeDato = meldekort.hode.meldeDato,
            lestDato = meldekort.hode.lestDato,
            sporsmal = mapOrdsSpmTilSporsmal(meldekort.spm),
            begrunnelse = meldekort.hode.kommentar
        )
    }

    private fun mapOrdsSpmTilSporsmal(spm: OrdsSpm): Sporsmal {
        return Sporsmal(
            arbeidssoker = spm.arbeidssoker.svarJa.verdi,
            arbeidet = spm.arbeidet.svarJa.verdi,
            syk = spm.syk.svarJa.verdi,
            annetFravaer = spm.annetFravaer.svarJa.verdi,
            kurs = spm.kurs.svarJa.verdi,
            forskudd = spm.forskudd.verdi,
            signatur = spm.signatur.verdi,
            meldekortDager = mapOrdsMeldekortDagerTilMeldekortDager(spm.meldekortDager)
        )
    }

    private fun mapOrdsMeldekortDagerTilMeldekortDager(ordsMeldekortDager: List<OrdsMeldekortDag>?): List<MeldekortDag>? {
        if (ordsMeldekortDager == null) {
            return null
        }
        val meldekortDagListe = mutableListOf<MeldekortDag>()
        ordsMeldekortDager.forEach {
            meldekortDagListe.add(
                MeldekortDag(
                    dag = it.dag,
                    arbeidetTimerSum = it.arbeidetTimerSum.verdi,
                    syk = it.syk.verdi,
                    annetFravaer = it.annetFravaer.verdi,
                    kurs = it.kurs.verdi
                )
            )
        }
        return meldekortDagListe
    }
}