package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.database.feil.UnretriableDatabaseException
import no.nav.meldeplikt.meldekortservice.model.meldekort.Meldekort
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.service.DBService
import java.sql.SQLException

object MeldekortMapper {

    fun filtrerMeldekortliste(person: Person, dbService: DBService): Person {
        return if (person.meldekortListe.isNullOrEmpty()) {
            person
        } else {
            person.copy(
                meldekortListe = fjernTidligereInnsendteMeldekort(person.meldekortListe, dbService)
            )
        }
    }

    private fun fjernTidligereInnsendteMeldekort(
        meldekortListe: List<Meldekort>,
        dbService: DBService
    ) =
        meldekortListe.filter {
            !erMeldekortSendtInnTidligere(it.meldekortId, dbService)
        }

    private fun erMeldekortSendtInnTidligere(meldekortId: Long, dbService: DBService): Boolean {
        return try {
            dbService.hentInnsendtMeldekort(meldekortId)
            true
        } catch (se: SQLException) {
            if (se.message == "Found no rows") {
                false
            } else {
                throw UnretriableDatabaseException("Feil ved lesing av innsendte meldekort", se)
            }
        }
    }
}