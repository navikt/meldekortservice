package no.nav.meldeplikt.meldekortservice.service

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.*
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost

class DBService(private val database: Database) {

    suspend fun settInnInnsendtMeldekort(innsendtMeldekort: InnsendtMeldekort) {
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

    fun lagreJournalpostMeldekortPar(journalpostId: Long, meldekortId: Long) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    lagreJournalpostMeldekortPar(journalpostId, meldekortId)
                }
            }
        }
    }

    fun lagreJournalpost(journalpost: Journalpost) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    lagreJournalpost(journalpost)
                }
            }
        }
    }

    fun hentJournalpostData(): List<Triple<String, Journalpost, Int>> =
        runBlocking {
            database.dbQuery {
                hentJournalpostData()
            }
        }

    fun sletteJournalpostData(id: String) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    sletteJournalpostData(id)
                }
            }
        }
    }

    fun oppdaterJournalpost(id: String, retries: Int) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    oppdaterJournalpost(id, retries)
                }
            }
        }
    }
}