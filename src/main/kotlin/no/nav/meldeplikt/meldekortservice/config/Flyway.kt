package no.nav.meldeplikt.meldekortservice.config

import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.database.OracleDatabase
import no.nav.meldeplikt.meldekortservice.database.PostgreSqlDatabase
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import java.sql.DatabaseMetaData
import javax.sql.DataSource

object Flyway {

    fun configure(env: Environment): FluentConfiguration {
        val dataSource = createCorrectAdminDatasourceForEnvironment(env)

        return configure(dataSource)
    }

    fun configure(dataSource: DataSource): FluentConfiguration {
        val configBuilder = Flyway.configure()
        configBuilder.dataSource(dataSource)

        val commonMigrationFiles = "classpath:db/migration/common"
        val oracleMigrationFiles = "classpath:db/migration/oracle"
        val postgreSqlMigrationFiles = "classpath:db/migration/postgresql"

        val metaData: DatabaseMetaData = dataSource.connection.metaData
        val productName = metaData.databaseProductName

        if (productName == "PostgreSQL" || productName == "H2") {
            configBuilder.locations(commonMigrationFiles, postgreSqlMigrationFiles)
        } else {
            configBuilder.locations(commonMigrationFiles, oracleMigrationFiles)
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
