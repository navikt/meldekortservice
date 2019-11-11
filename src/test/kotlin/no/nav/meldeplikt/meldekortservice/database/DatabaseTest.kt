package no.nav.meldeplikt.meldekortservice.database

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DatabaseTest {

    private val database = H2Database()

    @Test
    fun testDB() {
        val test = runBlocking {
            database.dbQuery { getAllMessages() }
        }
        println(test)
        assertTrue { test.size == 1 }
    }
}