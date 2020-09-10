package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.database.feil.UnretriableDatabaseException
import no.nav.meldeplikt.meldekortservice.model.meldekort.Meldekort
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.service.InnsendtMeldekortService
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import java.sql.SQLException

object MeldekortMapper {

    fun filtrerMeldekortliste(person: Person, meldekortService: InnsendtMeldekortService): Person {
        return if (person.meldekortListe.isNullOrEmpty()) {
            person
        } else {
            person.copy(
                meldekortListe = fjernTidligereInnsendteMeldekort(person.meldekortListe, meldekortService)
            )
        }
    }

    private fun fjernTidligereInnsendteMeldekort(
        meldekortListe: List<Meldekort>,
        meldekortService: InnsendtMeldekortService
    ) =
        meldekortListe.filter {
            !erMeldekortSendtInnTidligere(it.meldekortId, meldekortService)
        }

    private fun erMeldekortSendtInnTidligere(meldekortId: Long, meldekortService: InnsendtMeldekortService): Boolean {
        return try {
            throw SQLException("test")
            meldekortService.hentInnsendtMeldekort(meldekortId)
            true
        } catch (se: SQLException) {
            if (se.message == "Found no rows") {
                false
            } else {
                val errorMessage =
                    ErrorMessage("Forsøkte å sjekke om meldekort med id ${meldekortId} er sendt inn tidligere, men klarte ikke å lese fra MIP-tabellen. ${se.message}")
                defaultLog.warn(errorMessage.error, se)
                //false
                throw UnretriableDatabaseException("Feil ved lesing av innsendte meldekort", se)
            }
        }
    }
}