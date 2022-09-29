package no.nav.meldeplikt.meldekortservice.service

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.*
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import java.sql.Connection

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

    fun lagreKallLogg(kallLogg: KallLogg): Long =
        runBlocking {
            database.dbQuery {
                lagreKallLogg(kallLogg)
            }
        }

    fun lagreResponse(kallLoggId: Long, status: Int, response: String) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    lagreResponse(kallLoggId, status, response)
                }
            }
        }
    }

    fun oppdaterStatus(kallLoggId: Long, status: Int) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    oppdaterStatus(kallLoggId, status)
                }
            }
        }
    }

    fun hentKallLoggFelterListeByKorrelasjonId(korrelasjonId: String): List<KallLogg> =
        runBlocking {
            database.dbQuery { hentKallLoggFelterListeByKorrelasjonId(korrelasjonId) }
        }

    fun getConnection(): Connection {
        return database.dataSource.connection
    }
}
