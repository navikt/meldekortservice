package no.nav.meldeplikt.meldekortservice.config

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTAuthenticationProvider
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.util.pipeline.PipelineContext
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import java.net.URL
import java.util.concurrent.TimeUnit

fun JWTAuthenticationProvider.Configuration.setupOidcAuthentication(environment: Environment) {
    val jwkProvider = Security.initJwkProvider(environment.securityJwksUri)
    verifier(jwkProvider, environment.securityJwksIssuer)
    realm = "meldekortservice"
    validate { credentials ->
        return@validate Security.validationLogicPerRequest(credentials, environment)
    }
}
// TODO Sette opp token support lokalt
fun PipelineContext<Unit, ApplicationCall>.extractIdentFromLoginContext(): String =
    if (isCurrentlyRunningOnNais()) (call.authentication.principal as JWTPrincipal).payload.subject else "11111111111"

object Security {

    fun initJwkProvider(securityJwksUri: URL): JwkProvider {
        return JwkProviderBuilder(securityJwksUri)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
    }

    fun validationLogicPerRequest(credentials: JWTCredential, environment: Environment): JWTPrincipal? {
        return when (isCorrectAudienceSet(credentials, environment)) {
            true -> JWTPrincipal(credentials.payload)
            false -> null
        }
    }

    private fun isCorrectAudienceSet(credentials: JWTCredential, environment: Environment) =
        credentials.payload.audience.contains(environment.securityAudience)
}