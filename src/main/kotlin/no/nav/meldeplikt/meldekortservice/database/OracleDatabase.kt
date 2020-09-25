package no.nav.meldeplikt.meldekortservice.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.utils.DB_ORACLE_USERNAME
import no.nav.meldeplikt.meldekortservice.utils.SBL_ARBEID_USERNAME
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil

class OracleDatabase(env: Environment) {

    private val envDataSource: HikariDataSource

    init {
        envDataSource = createConnectionViaVaultWithDbUser(env)
    }

    fun createConnectionViaVaultWithDbUser(env: Environment): HikariDataSource {
        return hikariDatasourceViaVault(env)
    }

    companion object {

        fun hikariDatasourceViaVault(env: Environment): HikariDataSource {
            val config = hikariCommonConfig(env).apply {
                username = env.dbUserOracle.username
                password = env.dbUserOracle.password
                validate()
            }
            return HikariDataSource(config)
        }

        private fun hikariCommonConfig(env: Environment): HikariConfig {
            val config = HikariConfig().apply {
                driverClassName = "oracle.jdbc.OracleDriver"
                jdbcUrl = env.dbConfOracle.jdbcUrl
                minimumIdle = 1
                maxLifetime = 30001
                maximumPoolSize = 3
                connectionTimeout = 500
                validationTimeout = 250
                idleTimeout = 10001
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            }
            return config
        }
    }
}