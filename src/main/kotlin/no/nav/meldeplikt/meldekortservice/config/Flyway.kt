package no.nav.meldeplikt.meldekortservice.config

import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.database.PostgresDatabase
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import javax.sql.DataSource

object Flyway {

    fun runFlywayMigrations(env: Environment) {
        val flyway = configure(env).load()
        flyway.migrate()
    }

    private fun configure(env: Environment): FluentConfiguration {
        val configBuilder = Flyway.configure()
        val dataSource = createCorrectAdminDatasourceForEnvironment(env)
        configBuilder.dataSource(dataSource)

        if (isCurrentlyRunningOnNais()) {
            configBuilder.initSql("SET ROLE \"${env.dbAdmin}\"")
        }
        return configBuilder
    }

    private fun createCorrectAdminDatasourceForEnvironment(env: Environment): DataSource {
        return when (isCurrentlyRunningOnNais()) {
            true -> createDataSourceViaVaultWithAdminUser(env)
            false -> createDataSourceForLocalDbWithAdminUser(env)
        }
    }

    private fun createDataSourceViaVaultWithAdminUser(env: Environment): HikariDataSource {
        return PostgresDatabase.hikariDatasourceViaVault(env, env.dbAdmin)
    }

    private fun createDataSourceForLocalDbWithAdminUser(env: Environment): HikariDataSource {
        return PostgresDatabase.hikariFromLocalDb(env, env.dbUser)
    }
}
