package no.nav.meldeplikt.meldekortservice.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.io.File
import java.net.URI
import java.net.URL

const val dbUserSecretPath = "/secrets/dbuser/meldekortservicedbuser"
const val dbConfigSecretPath = "/secrets/dbconf/meldekortservicedbconf"
const val DUMMY_URL = "https://dummyurl.nav.no"
const val DUMMY_FNR = "01020312345"
val DUMMY_TOKEN = JWT.create()
    .withClaim("pid", DUMMY_FNR)
    .withClaim("name", "Test Testesen")
    .withClaim("iat", 1616239022)
    .sign(Algorithm.none())
    .toString()

data class Environment(
    val ordsUrl: URL = URI.create(getEnvVar("ORDS_URI", DUMMY_URL)).toURL(),
    val ordsClientId: String = getEnvVar("CLIENT_ID", "cLiEnTiD"),
    val ordsClientSecret: String = getEnvVar("CLIENT_SECRET", "cLiEnTsEcReT"),

    // PostgreSQL
    val dbHostPostgreSQL: String = getEnvVar("DB_HOST", "localhost:5434"),
    val dbNamePostgreSQL: String = getEnvVar("DB_NAME", "meldekortservice-local"),
    val dbUserPostgreSQL: String = getEnvVar("DB_USERNAME", "meldekortservice-local"),
    val dbUrlPostgreSQL: String = "jdbc:postgresql://$dbHostPostgreSQL/$dbNamePostgreSQL",
    val dbPasswordPostgreSQL: String = getEnvVar("DB_PASSWORD", "testpassword"),

    // Oracle
    val dbUserOracle: VaultCredentials = VaultCredentials(
        File("$dbUserSecretPath/username").takeIf { it.exists() }?.readText() ?: "username",
        File("$dbUserSecretPath/password").takeIf { it.exists() }?.readText() ?: "password"
    ),
    val dbConfOracle: VaultDbConfig = VaultDbConfig(
        File("$dbConfigSecretPath/jdbc_url").takeIf { it.exists() }?.readText() ?: "jdbcUrl"
    ),
)

fun getEnvVar(varName: String, defaultValue: String? = null): String {
    return System.getenv(varName) ?: defaultValue
    ?: throw IllegalArgumentException("Variabelen $varName kan ikke være tom")
}

data class VaultCredentials(
    val username: String?,
    val password: String?
)

data class VaultDbConfig(
    val jdbcUrl: String?
)
