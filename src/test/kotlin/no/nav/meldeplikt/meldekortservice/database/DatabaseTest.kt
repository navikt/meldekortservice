package no.nav.meldeplikt.meldekortservice.database

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.SQLException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DatabaseTest {

    private val database = H2Database("dbtest")
    private val innsendtMeldekort1 = InnsendtMeldekort(1L)
    private val innsendtMeldekort2 = InnsendtMeldekort(2L)
    private val innsendtMeldekort3 = InnsendtMeldekort(3L)
    private val alleInnsendtMeldekort = listOf(innsendtMeldekort1, innsendtMeldekort2, innsendtMeldekort3)

    init {
        runBlocking {
            database.dbQuery {
                opprettInnsendtMeldekort(innsendtMeldekort1)
                opprettInnsendtMeldekort(innsendtMeldekort2)
                opprettInnsendtMeldekort(innsendtMeldekort3)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        database.closeConnection()
    }

    @Test
    fun `Henter ut alle innsendte meldekort`() {
        runBlocking {
            val result = database.dbQuery { hentAlleInnsendteMeldekort() }
            assertEquals(alleInnsendtMeldekort.size, result.size)
            assertContentEquals(alleInnsendtMeldekort, result)
        }
    }

    @Test
    fun `Henter ut et innsendt meldekort`() {
        runBlocking {
            val result = database.dbQuery { hentInnsendtMeldekort(innsendtMeldekort1.meldekortId) }
            assertEquals(innsendtMeldekort1.meldekortId, result.meldekortId)
        }
    }

    @Test
    fun `Henter ut et innsendt meldekort som ikke finnes`() {
        val exception = assertThrows<SQLException> {
            runBlocking {
                database.dbQuery { hentInnsendtMeldekort(123L) }
            }
        }
        Assertions.assertEquals("Found no rows", exception.localizedMessage)
    }
}
