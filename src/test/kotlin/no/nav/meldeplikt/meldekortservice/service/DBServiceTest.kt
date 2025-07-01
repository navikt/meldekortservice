package no.nav.meldeplikt.meldekortservice.service

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DBServiceTest {
    private val database = H2Database("dbservicetest")

    @AfterAll
    fun tearDown() {
        database.closeConnection()
    }

    @Test
    fun `skal lagre request og response`() {
        val dbService = DBService(database)
        val tidspunkt = LocalDateTime.now()
        val kallTid = Instant.now().toEpochMilli()

        runBlocking {
            val kallLogg = KallLogg(
                "korrelasjonId1",
                tidspunkt,
                "REST",
                "INN",
                "GET",
                "/meldekortservice/api/meldekort",
                0,
                kallTid,
                "ping",
                "",
                "test",
                "01020312345"
            )

            val kallLoggId = dbService.lagreKallLogg(kallLogg)
            checkKallLogg(dbService, kallLogg, 0, "", 0)

            val response = "pong"
            dbService.lagreResponse(kallLoggId, 200, response)
            checkKallLogg(dbService, kallLogg, 200, response, kallTid)
        }
    }

    private fun checkKallLogg(dbService: DBService, kallLogg: KallLogg, status: Int, response: String, kallTid: Long) {
        val kallLoggListe = dbService.hentKallLoggFelterListeByKorrelasjonId(kallLogg.korrelasjonId)
        assertEquals(1, kallLoggListe.size)
        assertEquals(kallLogg.korrelasjonId, kallLoggListe[0].korrelasjonId)
        assertEquals(kallLogg.tidspunkt, kallLoggListe[0].tidspunkt)
        assertEquals(kallLogg.type, kallLoggListe[0].type)
        assertEquals(kallLogg.kallRetning, kallLoggListe[0].kallRetning)
        assertEquals(kallLogg.method, kallLoggListe[0].method)
        assertEquals(kallLogg.operation, kallLoggListe[0].operation)
        assertEquals(status, kallLoggListe[0].status)
        assertTrue(kallLoggListe[0].kallTid in 1..Instant.now().toEpochMilli() - kallTid)
        assertEquals(kallLogg.request, kallLoggListe[0].request)
        assertEquals(response, kallLoggListe[0].response)
        assertEquals(kallLogg.logginfo, kallLoggListe[0].logginfo)
        assertEquals(kallLogg.ident, kallLoggListe[0].ident)
    }
}
