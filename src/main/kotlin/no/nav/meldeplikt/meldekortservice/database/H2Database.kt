package no.nav.meldeplikt.meldekortservice.database

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location

class H2Database : Database {

    private val memDataSource: HikariDataSource

    init {
        memDataSource = createDataSource()
        flyway()
    }

    override val dataSource: HikariDataSource
        get() = memDataSource

    private fun createDataSource(): HikariDataSource {
        return HikariDataSource().apply {
            jdbcUrl = "jdbc:h2:mem:testdb"
            username = "sa"
            password = ""

            validate()
        }
    }

    private fun flyway() {
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()
    }
}