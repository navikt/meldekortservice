package no.nav.meldeplikt.meldekortservice.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.utils.DB_ORACLE_CONF
import no.nav.meldeplikt.meldekortservice.utils.DB_ORACLE_PASSWORD
import no.nav.meldeplikt.meldekortservice.utils.DB_ORACLE_USERNAME

class OracleDatabase : Database {

    private val envDataSource: HikariDataSource

    init {
        envDataSource = createConnectionViaVaultWithDbUser()
    }

    private fun createConnectionViaVaultWithDbUser(): HikariDataSource {
        return hikariDatasourceViaVault()
    }

    override val dataSource: HikariDataSource
        get() = envDataSource

    companion object {

        fun hikariDatasourceViaVault(): HikariDataSource {
            val config = hikariCommonConfig().apply {
                username = System.getProperty(DB_ORACLE_USERNAME, "username")
                password = System.getProperty(DB_ORACLE_PASSWORD, "password")
                validate()
            }
            return HikariDataSource(config)
        }

        private fun hikariCommonConfig(): HikariConfig {
            return HikariConfig().apply {
                driverClassName = "oracle.jdbc.OracleDriver"
                jdbcUrl = System.getProperty(DB_ORACLE_CONF, "jdbcUrl")
                isAutoCommit = false
                connectionTimeout = 1000
                maxLifetime = 30001
                validationTimeout = 500
            }
        }
    }
}