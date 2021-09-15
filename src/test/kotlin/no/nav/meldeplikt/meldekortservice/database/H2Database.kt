package no.nav.meldeplikt.meldekortservice.database

import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.config.Flyway

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
        Flyway.configure(Environment())
            .dataSource(dataSource)
            .load()
            .migrate()
    }
}