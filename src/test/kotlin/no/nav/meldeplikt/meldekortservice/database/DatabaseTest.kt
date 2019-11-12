package no.nav.meldeplikt.meldekortservice.database

import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.database.Message
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DatabaseTest {

    private val database = H2Database()

    init {
        runBlocking {
            database.dbQuery {
                createMessage(Message(1, "Dette er en test"))
            }
        }
    }

    @Test
    fun testDB() {
        val test = runBlocking {
            database.dbQuery { getAllMessages() }
        }
        println(test)
        assertTrue { test.size == 1 }
    }
}