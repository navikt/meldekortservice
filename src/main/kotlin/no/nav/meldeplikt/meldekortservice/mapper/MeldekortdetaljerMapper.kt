package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.MeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Meldekort as ArenaMeldekort
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Spm as ArenaSpm
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.MeldekortDag as ArenaMeldekortDag

object MeldekortdetaljerMapper {

    fun mapOrdsMeldekortTilMeldekortdetaljer(meldekort: ArenaMeldekort): Meldekortdetaljer {
        defaultLog.info("TODO behold? MeldekortdetaljerMapper.mapOrdsMeldekortTilMeldekortdetaljer meldekort id = ${meldekort.hode.meldekortId.verdi}")
        return Meldekortdetaljer(
            id = "1",
            personId = meldekort.hode.personId.verdi,
            fodselsnr = meldekort.hode.fodselsnr.verdi,
            meldekortId = meldekort.hode.meldekortId.verdi,
            meldeperiode = meldekort.hode.meldeperiode,
            meldegruppe = meldekort.hode.meldegruppe ?: "",
            arkivnokkel = meldekort.hode.arkivnokkel,
            kortType = KortType.getByCode(meldekort.hode.kortType),
            meldeDato = meldekort.hode.meldeDato,
            lestDato = meldekort.hode.lestDato,
            sporsmal = mapOrdsSpmTilSporsmal(meldekort.spm),
            begrunnelse = meldekort.hode.kommentar
        )
    }

    private fun mapOrdsSpmTilSporsmal(spm: ArenaSpm): Sporsmal {
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

    private fun mapOrdsMeldekortDagerTilMeldekortDager(arenaMeldekortDager: List<ArenaMeldekortDag>?): List<MeldekortDag>? {
        if (arenaMeldekortDager == null) {
            return null
        }
        val meldekortDagListe = mutableListOf<MeldekortDag>()
        arenaMeldekortDager.forEach {
            meldekortDagListe.add(
                MeldekortDag(
                    dag = it.dag,
                    arbeidetTimerSum = konverterFraStringTilFloat(it.arbeidetTimerSum.verdi),
                    syk = it.syk.verdi,
                    annetFravaer = it.annetFravaer.verdi,
                    kurs = it.kurs.verdi
                )
            )
        }
        return meldekortDagListe
    }

    private fun konverterFraStringTilFloat(s: String): Float =
        (if (s.contains(',')) s.replace(',', '.') else s).toFloat()
}