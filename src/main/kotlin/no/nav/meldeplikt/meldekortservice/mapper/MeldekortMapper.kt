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

    private fun fjernTidligereInnsendteMeldekort(meldekortListe: List<Meldekort>, meldekortService: InnsendtMeldekortService) =
        meldekortListe.filter {
            !erMeldekortSendtInnTidligere(it.meldekortId, meldekortService)
        }

    private fun erMeldekortSendtInnTidligere(meldekortId: Long, meldekortService: InnsendtMeldekortService): Boolean {
        return try {
            // test
            throw SQLException("Klarer ikke å lese innsendte meldekort fra MIP-tabellen")
            meldekortService.hentInnsendtMeldekort(meldekortId)
            true
        } catch (se: SQLException) {
            if (se.message == "Found no rows") {
                false
            } else {
                // Test
                val errorMessage =
                    ErrorMessage("Feil ved lesing fra MIP-tabellen. Klarer ikke å sjekke om meldekort med id ${meldekortId} er sendt inn tidligere. ${se.message}")
                defaultLog.warn(errorMessage.error, se)
                throw UnretriableDatabaseException("Feil ved lesing av innsendte meldekort", se)
            }
        }
    }
}