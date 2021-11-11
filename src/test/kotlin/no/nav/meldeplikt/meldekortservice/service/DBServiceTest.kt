package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.*
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import org.amshove.kluent.`with message`
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.SQLException

class DBServiceTest {
    private val database = H2Database()
    private val innsendtMeldekort1 = InnsendtMeldekort(1L)

    @AfterAll
    fun tearDown() {
        database.closeConnection()
    }

    @Test
    fun `test settInn og hent av meldekort `() {
        val dbService = DBService(database)

        runBlocking {
            dbService.settInnInnsendtMeldekort(innsendtMeldekort1)
            val actualMeldekort = dbService.hentInnsendtMeldekort(1L)
            assertEquals(innsendtMeldekort1.meldekortId, actualMeldekort.meldekortId)
        }
    }

    @Test
    fun `test hent av meldekort throws Exception `() {
        val dbService = DBService(database)

        invoking {
            runBlocking {
                dbService.hentInnsendtMeldekort(2L)
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `test lagre, hente, slette og oppdater midlertidig lagrede journalposter`() {
        val dbService = DBService(database)
        val connection = dbService.getConnection()

        val journalpostJson = this::class.java.getResource("/journalpost.json")!!.readText()
        val journalpost = jacksonObjectMapper().readValue(
            journalpostJson,
            Journalpost::class.java
        )
        val journalpost1 = journalpost.copy(eksternReferanseId = "11")
        val journalpost2 = journalpost.copy(eksternReferanseId = "22")

        runBlocking {
            // Lagre
            dbService.lagreJournalpostMidlertidig(journalpost1)
            dbService.lagreJournalpostMidlertidig(journalpost2)

            // Hente
            var journalpostData = connection.hentMidlertidigLagredeJournalposter()
            assertEquals(2, journalpostData.size)
            assertEquals(1, journalpostData.filter { it.first == "11" }.size)
            assertEquals(1, journalpostData.filter { it.first == "22" }.size)

            // Slette
            connection.sletteMidlertidigLagretJournalpost("11")

            // Oppdater
            connection.oppdaterMidlertidigLagretJournalpost("22", 5)

            // Hente
            journalpostData = connection.hentMidlertidigLagredeJournalposter()
            assertEquals(1, journalpostData.size)
            val data = journalpostData.first { it.first == "22" }
            assertEquals(5, data.third)
        }
    }

    @Test
    fun `test lagre journalpost data`() {
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
}