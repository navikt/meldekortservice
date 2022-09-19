package no.nav.meldeplikt.meldekortservice.config

import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.service.DBService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoggingPluginTest {

    private lateinit var database: H2Database
    private lateinit var dbService: DBService

    @BeforeEach
    fun setUp() {
        database = H2Database("loggingplugintest")
        dbService = DBService(database)
    }

    @AfterEach
    fun tearDown() {
        database.closeConnection()
    }

    @Test
    fun `skal lagre request og response`() {

    }
}
