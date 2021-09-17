package no.nav.meldeplikt.meldekortservice.database

import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.config.Flyway

class H2Database : Database {

    override val dataSource: HikariDataSource = HikariDataSource().apply {
        jdbcUrl = "jdbc:h2:mem:testdb"
        username = "sa"
        password = ""

        validate()
    }

    init {
        Flyway.configure(dataSource).load().migrate()
    }

    fun closeConnection() {
        dataSource.close()
    }
}