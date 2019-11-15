package no.nav.meldeplikt.meldekortservice.database

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class DatabaseTest {

    private val database = H2Database()
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
        runBlocking {
            database.dbQuery {
                slettAlleInnsendteMeldekort()
            }
        }
    }

    @Test
    fun `Henter ut alle innsendte meldekort`() {
        runBlocking {
            val result = database.dbQuery { hentAlleInnsendteMeldekort() }
            result.size `should be equal to` alleInnsendtMeldekort.size
            result `should contain all` alleInnsendtMeldekort
        }
    }

    @Test
    fun `Henter ut et innsendt meldekort`() {
        runBlocking {
            val result = database.dbQuery { hentInnsendtMeldekort(innsendtMeldekort1.meldekortId) }
            result.meldekortId `should be equal to` innsendtMeldekort1.meldekortId
        }
    }

    @Test
    fun `Henter ut et innsendt meldekort som ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { hentInnsendtMeldekort(123L) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }
}