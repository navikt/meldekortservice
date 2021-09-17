package no.nav.meldeplikt.meldekortservice.service

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import org.amshove.kluent.`with message`
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.SQLException

class InnsendtMeldekortServiceTest {
    private val database = H2Database()
    private val innsendtMeldekort1 = InnsendtMeldekort(1L)

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.closeConnection()
        }
    }

    @Test
    fun `test settInn og hent av meldekort `() {
        val innsendtMeldekortService = InnsendtMeldekortService(database = database)

        runBlocking {
            innsendtMeldekortService.settInnInnsendtMeldekort(innsendtMeldekort1)
            val actualMeldekort = innsendtMeldekortService.hentInnsendtMeldekort(1L)
            assertEquals(innsendtMeldekort1.meldekortId, actualMeldekort.meldekortId)
        }
    }

    @Test
    fun `test hent av meldekort throws Exception `() {
        val innsendtMeldekortService = InnsendtMeldekortService(database = database)

        invoking {
            runBlocking {
                innsendtMeldekortService.hentInnsendtMeldekort(2L)
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }
}