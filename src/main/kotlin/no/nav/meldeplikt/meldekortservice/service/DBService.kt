package no.nav.meldeplikt.meldekortservice.service

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.Database
import no.nav.meldeplikt.meldekortservice.database.hentKallLoggFelterListeByKorrelasjonId
import no.nav.meldeplikt.meldekortservice.database.lagreKallLogg
import no.nav.meldeplikt.meldekortservice.database.lagreResponse
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import java.sql.Connection

class DBService(private val database: Database) {

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

    fun hentKallLoggFelterListeByKorrelasjonId(korrelasjonId: String): List<KallLogg> =
        runBlocking {
            database.dbQuery { hentKallLoggFelterListeByKorrelasjonId(korrelasjonId) }
        }

    fun getConnection(): Connection {
        return database.dataSource.connection
    }
}
