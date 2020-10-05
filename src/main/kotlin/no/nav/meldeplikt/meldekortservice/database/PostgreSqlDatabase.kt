package no.nav.meldeplikt.meldekortservice.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.meldeplikt.meldekortservice.config.Environment

class PostgreSqlDatabase(env: Environment) : Database {

    private val envDataSource: HikariDataSource

    init {
        envDataSource = createConnectionForLocalDbWithDbUser(env)
    }

    override val dataSource: HikariDataSource
        get() = envDataSource

    private fun createConnectionForLocalDbWithDbUser(env: Environment): HikariDataSource {
        return hikariFromLocalDb(env, env.dbUserPostgreSQL)
    }

    companion object {

        fun hikariFromLocalDb(env: Environment, dbUser: String): HikariDataSource {
            val config = hikariCommonConfig(env).apply {
                username = dbUser
                password = env.dbPasswordPostgreSQL
                validate()
            }
            return HikariDataSource(config)
        }

        private fun hikariCommonConfig(env: Environment): HikariConfig {
            val config = HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = env.dbUrlPostgreSQL
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