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

    fun lagreJournalpostData(journalpostId: Long, dokumentInfoId: Long, meldekortId: Long) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    lagreJournalpostData(journalpostId, dokumentInfoId, meldekortId)
                }
            }
        }
    }

    fun lagreJournalpostMidlertidig(journalpost: Journalpost) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    lagreJournalpostMidlertidig(journalpost)
                }
            }
        }
    }

    fun hentMidlertidigLagredeJournalposter(): List<Triple<String, Journalpost, Int>> =
        runBlocking {
            database.dbQuery {
                hentMidlertidigLagredeJournalposter()
            }
        }

    fun sletteMidlertidigLagretJournalpost(id: String) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    sletteMidlertidigLagretJournalpost(id)
                }
            }
        }
    }

    fun oppdaterMidlertidigLagretJournalpost(id: String, retries: Int) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    oppdaterMidlertidigLagretJournalpost(id, retries)
                }
            }
        }
    }
}