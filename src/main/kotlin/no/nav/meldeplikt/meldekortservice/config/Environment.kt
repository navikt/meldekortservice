package no.nav.meldeplikt.meldekortservice.config

import com.bettercloud.vault.SslConfig
import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import com.bettercloud.vault.json.Json
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.meldeplikt.meldekortservice.utils.vaultKvPath
import no.nav.meldeplikt.meldekortservice.utils.vaultTokenPath
import no.nav.meldeplikt.meldekortservice.utils.vaultUrl
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

data class Environment(
    val username: String = getEnvVar("FSS_SYSTEMUSER_USERNAME", "username"),
    val password: String = getEnvVar("FSS_SYSTEMUSER_PASSWORD", "password"),
    val securityAudience: String = getEnvVar("AUDIENCE", "dummyAudience"),
    val securityJwksIssuer: String = getEnvVar("JWKS_ISSUER", "dummyIssuer"),
    val securityJwksUri: URL = URL(getEnvVar("JWKS_URI", "https://dummyUrl.com")),
    val ameldingUrl: URL = URL(getEnvVar("AMELDING_URI", "https://dummyUrl.com/path")),
    val personinfoUsername: String = getEnvVar("PERSONINFO_SERVICE_USERNAME", "username"),
    val personinfoPassword: String = getEnvVar("PERSONINFO_SERVICE_PASSWORD", "password"),
    val ordsUrl: URL = URL(getEnvVar("ORDS_URI", "https://dummyUrl.com")),
    val ordsClientId: String = getEnvVar("CLIENT_ID", "cLiEnTiD"),
    val ordsClientSecret: String = getEnvVar("CLIENT_SECRET", "cLiEnTsEcReT"),
    val securityTokenService: String = getEnvVar("SECURITYTOKENSERVICE", "https://dummyUrl.com"),
    val arbeidOgAktivitetUri: String = getEnvVar("ARBEIDOGAKTIVITET_URI", "https://dummyUrl.com")
)

fun getEnvVar(varName: String, defaultValue: String? = null): String {
    println(System.getenv())
    return System.getenv(varName) ?: defaultValue
    ?: throw IllegalArgumentException("Variabelen $varName kan ikke være tom")
}

private fun vault() = Vault(VaultConfig()
    .address(vaultUrl)
    .token(String(Files.readAllBytes(Paths.get(vaultTokenPath))))
    .openTimeout(5)
    .readTimeout(30)
    .sslConfig(SslConfig().build())
    .build()
)

fun hentVaultCredentials(): VaultCredentials {
    return if(isCurrentlyRunningOnNais()) {
        val credentials = Json.parse(vault().logical().read(vaultKvPath).data["data"]).asObject()
        VaultCredentials(credentials.get("password").asString(), credentials.get("username").asString())
    } else {
        VaultCredentials("test", "test")
    }
}

data class VaultCredentials(
    val username: String?,
    val password: String?
)
