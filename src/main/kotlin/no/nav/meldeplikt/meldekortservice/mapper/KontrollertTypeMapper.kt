package no.nav.meldeplikt.meldekortservice.mapper

import no.aetat.arena.mk_meldekort_kontrollert.ArsakskodeType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortDagType
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse

class KontrollertTypeMapper {

    fun mapKontrollResponseToKontrollertType(message: KontrollResponse): MeldekortKontrollertType {
        var kontroll = MeldekortKontrollertType()
        kontroll.meldekortId = message.meldekortId
        kontroll.arsakskoder = trekkutArsakskoder(message)
        kontroll.meldekortDager = trekkutMeldekortDager(message)
        kontroll.status = when (kontroll.arsakskoder.arsakskode.size) {
            0 -> "OK"
            else -> "FEIL"
        }
        return kontroll
    }

    private fun trekkutArsakskoder(message: KontrollResponse): MeldekortKontrollertType.Arsakskoder {
        var uttrekk = MeldekortKontrollertType.Arsakskoder()
        message.feilListe.forEach {
            var item = ArsakskodeType()
            item.kode = it.kode
            item.tekst = it.tekst
            uttrekk.arsakskode.add(item)
        }
        return uttrekk
    }

    private fun trekkutMeldekortDager(message: KontrollResponse): MeldekortKontrollertType.MeldekortDager {
        var uttrekk = MeldekortKontrollertType.MeldekortDager()
        message.feilListe.forEach {
            var item = MeldekortDagType()
            item.dag = it.dag!!
            uttrekk.meldekortDag.add(item)
        }
        return uttrekk
    }

}
