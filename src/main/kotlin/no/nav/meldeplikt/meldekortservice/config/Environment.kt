package no.nav.meldeplikt.meldekortservice.config

import java.net.URL

private const val DUMMY_URL: String = "https://dummyUrl.com"

data class Environment(
    val ameldingUrl: URL = URL(getEnvVar("AMELDING_URI", "$DUMMY_URL/path")),
    val ordsUrl: URL = URL(getEnvVar("ORDS_URI", DUMMY_URL)),
    val ordsClientId: String = getEnvVar("CLIENT_ID", "cLiEnTiD"),
    val ordsClientSecret: String = getEnvVar("CLIENT_SECRET", "cLiEnTsEcReT"),
    val securityTokenService: String = getEnvVar("SECURITYTOKENSERVICE", DUMMY_URL),
    val sakOgAktivitetUrl: String = getEnvVar("SAKOGAKTIVITET_URI", DUMMY_URL),

    // Meldekort-kontroll
    // TODO: Legge inn URL for meldekort-kontroll i prod i Vault, når URLen er kjent
    val meldekortKontrollUrl: String = getEnvVar("KONTROLL_URI", DUMMY_URL),
    val meldekortKontrollClientid: String = getEnvVar("KONTROLL_CLIENT_ID", "test"),

    // Azure AD
    val oauthClientId: String = getEnvVar("AZURE_CLIENT_ID", "test"),
    val oauthJwk: String = getEnvVar("AZURE_JWK", "test"),
    val oauthClientSecret: String = getEnvVar("AZURE_CLIENT_SECRET", "test"),
    val oauthEndpoint: String = getEnvVar("KONTROLL_OAUTH_ENDPOINT", "test"),
    val oauthTenant: String = getEnvVar("KONTROLL_OAUTH_TENANT_ID", "test"),

    // PostgreSQL
    val dbHostPostgreSQL: String = getEnvVar("DB_HOST", "localhost:5432"),
    val dbNamePostgreSQL: String = getEnvVar("DB_NAME", "meldeplikt"),
    val dbUserPostgreSQL: String = getEnvVar("DB_NAME", "test") + "-user",
    val dbUrlPostgreSQL: String = "jdbc:postgresql://$dbHostPostgreSQL/$dbNamePostgreSQL",
    val dbPasswordPostgreSQL: String = getEnvVar("DB_PASSWORD", "testpassword"),

    // Oracle
    val dbUserOracle: VaultCredentials = VaultCredentials(
        getEnvVar("DB_USER_MELDEKORTSERVICE_USERNAME", "username"),
        getEnvVar("DB_USER_MELDEKORTSERVICE_PASSWORD", "password")
    ),
    val dbConfOracle: VaultDbConfig = VaultDbConfig(getEnvVar("DB_CONFIG_MELDEKORTSERVICE_JDBCURL", "jdbcUrl")),

    // Serviceusers
    val srvMeldekortservice: VaultCredentials = VaultCredentials(
        getEnvVar("SERVICEUSER_MELDEKORTSERVICE_USERNAME", "username"),
        getEnvVar("SERVICEUSER_MELDEKORTSERVICE_PASSWORD", "password")
    ),
    val srvSblArbeid: VaultCredentials = VaultCredentials(
        getEnvVar("SERVICEUSER_SBLARBEID_USERNAME", "username"),
        getEnvVar("SERVICEUSER_SBLARBEID_PASSWORD", "password")
    )
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
