package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.*
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DBServiceTest {
    private val database = H2Database("dbservicetest")
    private val innsendtMeldekort1 = InnsendtMeldekort(1L)

    @AfterAll
    fun tearDown() {
        database.closeConnection()
    }

    @Test
    fun `skal lagre og hente innsendt meldekort `() {
        val dbService = DBService(database)

        runBlocking {
            dbService.settInnInnsendtMeldekort(innsendtMeldekort1)
            val actualMeldekort = dbService.hentInnsendtMeldekort(1L)
            assertEquals(innsendtMeldekort1.meldekortId, actualMeldekort.meldekortId)
        }
    }

    @Test
    fun `skal kaste Exception hvis henter ikke eksisterende innsendt meldekort`() {
        val dbService = DBService(database)

        val exception = assertThrows<SQLException> {
            runBlocking {
                dbService.hentInnsendtMeldekort(2L)
            }
        }
        assertEquals("Found no rows", exception.localizedMessage)
    }

    @Test
    fun `skal lagre, hente, slette og oppdatere midlertidig lagrede journalposter`() {
        val dbService = DBService(database)
        val connection = dbService.getConnection()

        val journalpostJson = this::class.java.getResource("/journalpost.json")!!.readText()
        val journalpost = jacksonObjectMapper().readValue(
            journalpostJson,
            Journalpost::class.java
        )
        val id1 = "123456789012345678901234567890123456"
        val id2 = "223456789012345678901234567890123456"
        val journalpost1 = journalpost.copy(eksternReferanseId = id1)
        val journalpost2 = journalpost.copy(eksternReferanseId = id2)

        runBlocking {
            // Lagre
            dbService.lagreJournalpostMidlertidig(journalpost1)
            dbService.lagreJournalpostMidlertidig(journalpost2)

            // Hente
            var journalpostData = connection.hentMidlertidigLagredeJournalposter()
            assertEquals(2, journalpostData.size)
            assertEquals(1, journalpostData.filter { it.first == id1 }.size)
            assertEquals(1, journalpostData.filter { it.first == id2 }.size)

            // Slette
            connection.sletteMidlertidigLagretJournalpost(id1)

            // Oppdater
            connection.oppdaterMidlertidigLagretJournalpost(id2, 5)

            // Hente
            journalpostData = connection.hentMidlertidigLagredeJournalposter()
            assertEquals(1, journalpostData.size)
            val data = journalpostData.first { it.first == id2 }
            assertEquals(5, data.third)
        }
    }

    @Test
    fun `skal lagre journalpost data`() {
        val dbService = DBService(database)

        runBlocking {
            val result = database.dbQuery { hentJournalpostData() }
            assertEquals(0, result.size)
        }

        dbService.lagreJournalpostData(123L, 223L, 323L)

        runBlocking {
            val result = database.dbQuery { hentJournalpostData() }
            assertEquals(1, result.size)
        }
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
                null,
                "test"
            )

            dbService.lagreRequest(kallLogg)
            checkKallLogg(dbService, kallLogg, 0, "", 0)

            val response = "pong"
            dbService.lagreResponse(kallLogg.korrelasjonId, 200, response)
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
    }
}
