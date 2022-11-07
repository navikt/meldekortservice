package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.ArsakskodeType
import no.nav.meldeplikt.meldekortservice.model.MeldekortDagKontrollertType
import no.nav.meldeplikt.meldekortservice.model.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse

class KontrollertTypeMapper {

    // Vi ignorerer kontrollstatus her
    fun mapKontrollResponseToKontrollertType(message: KontrollResponse): MeldekortKontrollertType {
        val kontroll = MeldekortKontrollertType()
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
        val uttrekk = MeldekortKontrollertType.Arsakskoder()
        message.feilListe.forEach {
            val item = ArsakskodeType()
            item.kode = it.kode
            item.tekst = it.tekst
            item.params = it.params
            uttrekk.arsakskode.add(item)
        }
        return uttrekk
    }

    private fun trekkutMeldekortDager(message: KontrollResponse): MeldekortKontrollertType.MeldekortDager {
        val uttrekk = MeldekortKontrollertType.MeldekortDager()
        message.feilListe.forEach {
            val item = MeldekortDagKontrollertType()
            item.dag = it.dag!!
            uttrekk.meldekortDag.add(item)
        }
        return uttrekk
    }
}
