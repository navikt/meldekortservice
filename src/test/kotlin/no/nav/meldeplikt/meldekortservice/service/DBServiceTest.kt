package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.stats.haveVariance
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
import kotlin.test.assertTrue

class DBServiceTest {
    private val database = H2Database()
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

        invoking {
            runBlocking {
                dbService.hentInnsendtMeldekort(2L)
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
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
    fun `skal returnere tekst hvis eksisterer`() {
        val dbService = DBService(database)

        var result = dbService.hentTekst("diverse.tilbake", "nb", "1000-01-01")
        assertEquals("Til Ditt NAV", result)

        result = dbService.hentTekst("diverse.tilbake", "en", "1000-01-01")
        assertEquals("To Your page", result)
    }

    @Test
    fun `skal returnere null hvis tekst ikke eksisterer`() {
        val dbService = DBService(database)

        var result = dbService.hentTekst("eksisterer_ikke", "nb", "1000-01-01")
        assertEquals(null, result)

        result = dbService.hentTekst("eksisterer_ikke", "en", "1000-01-01")
        assertEquals(null, result)
    }

    @Test
    fun `skal returnere tekster`() {
        val dbService = DBService(database)

        var result = dbService.hentAlleTekster("nb", "1000-01-01")
        assertTrue(result.size > 1)

        result = dbService.hentAlleTekster("en", "1000-01-01")
        assertTrue(result.size > 1)
    }
}