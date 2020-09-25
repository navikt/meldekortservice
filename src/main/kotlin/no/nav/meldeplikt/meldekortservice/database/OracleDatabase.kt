package no.nav.meldeplikt.meldekortservice.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.config.Environment

class OracleDatabase(env: Environment) : Database {

    private val envDataSource: HikariDataSource

    init {
        envDataSource = createConnectionViaVaultWithDbUser(env)
    }

    private fun createConnectionViaVaultWithDbUser(env: Environment): HikariDataSource {
        return hikariDatasourceViaVault(env)
    }

    override val dataSource: HikariDataSource
        get() = envDataSource

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
            return HikariConfig().apply {
                driverClassName = "oracle.jdbc.OracleDriver"
                jdbcUrl = env.dbConfOracle.jdbcUrl
                isAutoCommit = false
                connectionTimeout = 1000
                maxLifetime = 30001
                validationTimeout = 500
            }
        }
    }
}