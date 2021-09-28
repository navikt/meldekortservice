package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.database.hentJournalpostData
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

        val journalpostJson = this::class.java.getResource("/journalpost.json")!!.readText()
        val journalpost = jacksonObjectMapper().readValue(
            journalpostJson,
            Journalpost::class.java
        )
        val journalpost1 = journalpost.copy(eksternReferanseId = "1")
        val journalpost2 = journalpost.copy(eksternReferanseId = "2")

        runBlocking {
            // Lagre
            dbService.lagreJournalpostMidlertidig(journalpost1)
            dbService.lagreJournalpostMidlertidig(journalpost2)

            // Hente
            var journalpostData = dbService.hentMidlertidigLagredeJournalposter()
            assertEquals(2, journalpostData.size)
            assertEquals(1, journalpostData.filter { it.first == "1" }.size)
            assertEquals(1, journalpostData.filter { it.first == "2" }.size)

            // Slette
            dbService.sletteMidlertidigLagretJournalpost("1")

            // Oppdater
            dbService.oppdaterMidlertidigLagretJournalpost("2", 5)

            // Hente
            journalpostData = dbService.hentMidlertidigLagredeJournalposter()
            assertEquals(1, journalpostData.size)
            val data = journalpostData.first { it.first == "2" }
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