package no.nav.meldeplikt.meldekortservice.config

import com.bettercloud.vault.SslConfig
import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import com.bettercloud.vault.VaultException
import com.bettercloud.vault.json.Json
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.meldeplikt.meldekortservice.utils.vaultTokenPath
import no.nav.meldeplikt.meldekortservice.utils.vaultUrl
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

data class Environment(
    val username: String = getEnvVar("FSS_SYSTEMUSER_USERNAME", "username"),
    val password: String = getEnvVar("FSS_SYSTEMUSER_PASSWORD", "password"),
    val ameldingUrl: URL = URL(getEnvVar("AMELDING_URI", "https://dummyUrl.com/path")),
    val ordsUrl: URL = URL(getEnvVar("ORDS_URI", "https://dummyUrl.com")),
    val ordsClientId: String = getEnvVar("CLIENT_ID", "cLiEnTiD"),
    val ordsClientSecret: String = getEnvVar("CLIENT_SECRET", "cLiEnTsEcReT"),
    val securityTokenService: String = getEnvVar("SECURITYTOKENSERVICE", "https://dummyUrl.com"),
    val sakOgAktivitetUrl: String = getEnvVar("SAKOGAKTIVITET_URI", "https://dummyUrl.com"),

    // PostgreSQL
    val dbHostPostgreSQL: String = getEnvVar("DB_HOST", "localhost:5432"),
    val dbNamePostgreSQL: String = getEnvVar("DB_NAME", "meldeplikt"),
    val dbUserPostgreSQL: String = getEnvVar("DB_NAME", "test") + "-user",
    val dbUrlPostgreSQL: String = "jdbc:postgresql://$dbHostPostgreSQL/$dbNamePostgreSQL",
    val dbPasswordPostgreSQL: String = getEnvVar("DB_PASSWORD", "testpassword"),

    // Oracle
    val dbUserOracleKvPath: String = getEnvVar("MELDEKORTSERVICE_DS_CREDS_KV_PATH", "path"),
    val dbConfOracleKvPath: String = getEnvVar("MELDEKORTSERVICE_DS_CONF_KV_PATH", "path"),

    // Serviceusers
    val serviceUserKvPath: String = getEnvVar("SERVICE_USER_KV_PATH", "path"),
    val srvSblArbeidPath: String = getEnvVar("SRV_SBL_ARBEID_PATH", "path")
)

fun getEnvVar(varName: String, defaultValue: String? = null): String {
    return System.getenv(varName) ?: defaultValue
    ?: throw IllegalArgumentException("Variabelen $varName kan ikke være tom")
}

private fun vault() = Vault(
    VaultConfig()
        .address(vaultUrl)
        .token(String(Files.readAllBytes(Paths.get(vaultTokenPath))))
        .openTimeout(5)
        .readTimeout(30)
        .sslConfig(SslConfig().build())
        .build()
)

fun hentVaultCredentials(path: String): VaultCredentials {
    return if (isCurrentlyRunningOnNais()) {
        try {
            val credentials = Json.parse(vault().logical().read(path).data["data"]).asObject()
            VaultCredentials(credentials.get("username").asString(), credentials.get("password").asString())
        } catch (e: VaultException) {
            when (e.httpStatusCode) {
                403 -> defaultLog.error("Vault denied permission to fetch credentials for path '$path'", e)
                else -> defaultLog.error("Could not fetch credentials for path '$path'", e)
            }
            throw e
        }
    } else {
        VaultCredentials("test", "test")
    }
}

data class VaultCredentials(
    val username: String?,
    val password: String?
)

fun hentVaultDbConfig(path: String): VaultDbConfig {
    return if (isCurrentlyRunningOnNais()) {
        try {
            val config = Json.parse(vault().logical().read(path).data["data"]).asObject()
            VaultDbConfig(config.get("jdbc_url").asString())
        } catch (e: VaultException) {
            when (e.httpStatusCode) {
                403 -> defaultLog.error("Vault denied permission to fetch database configuration for path '$path'", e)
                else -> defaultLog.error("Could not fetch database configuration for path '$path'", e)
            }
            throw e
        }
    } else {
        VaultDbConfig("test")
    }
}

data class VaultDbConfig(
    val jdbcUrl: String?
)
