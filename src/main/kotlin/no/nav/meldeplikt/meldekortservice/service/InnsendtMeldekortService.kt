package no.nav.meldeplikt.meldekortservice.service

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.Database
import no.nav.meldeplikt.meldekortservice.database.hentInnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.database.opprettInnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort

class InnsendtMeldekortService(private val database: Database) {

    fun settInnInnsendtMeldekort(innsendtMeldekort: InnsendtMeldekort) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    opprettInnsendtMeldekort(innsendtMeldekort)
                }
            }
        }
    }

    fun hentInnsendtMeldekort(meldekortId: Long): InnsendtMeldekort =
        runBlocking {
            database.dbQuery { hentInnsendtMeldekort(meldekortId) }
        }
}