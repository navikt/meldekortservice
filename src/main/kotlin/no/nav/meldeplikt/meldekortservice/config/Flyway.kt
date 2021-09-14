package no.nav.meldeplikt.meldekortservice.config

import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.database.OracleDatabase
import no.nav.meldeplikt.meldekortservice.database.PostgreSqlDatabase
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import javax.sql.DataSource

object Flyway {

    fun runFlywayMigrations(env: Environment) {
        val flyway = configure(env).load()
        flyway.migrate()
    }

    fun configure(env: Environment): FluentConfiguration {
        val configBuilder = Flyway.configure()
        val dataSource = createCorrectAdminDatasourceForEnvironment(env)
        configBuilder.dataSource(dataSource)

        val commonMigrationFiles = "filesystem:src/main/resources/db/migration/common"
        val oracleMigrationFiles = "filesystem:src/main/resources/db/migration/oracle"
        val postgreSqlMigrationFiles = "filesystem:src/main/resources/db/migration/postgresql"

        if (isCurrentlyRunningOnNais()) {
            configBuilder.locations(commonMigrationFiles, oracleMigrationFiles)
        } else {
            configBuilder.locations(commonMigrationFiles, postgreSqlMigrationFiles)
        }

        return configBuilder
    }

    private fun createCorrectAdminDatasourceForEnvironment(env: Environment): DataSource {
        return when (isCurrentlyRunningOnNais()) {
            true -> createDataSourceViaVault()
            false -> createDataSourceForLocalDbWithAdminUser(env)
        }
    }

    private fun createDataSourceViaVault(): HikariDataSource {
        return OracleDatabase.hikariDatasourceViaVault()
    }

    private fun createDataSourceForLocalDbWithAdminUser(env: Environment): HikariDataSource {
        return PostgreSqlDatabase.hikariFromLocalDb(env, env.dbUserPostgreSQL)
    }
}
