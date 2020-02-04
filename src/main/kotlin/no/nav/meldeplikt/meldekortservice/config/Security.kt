package no.nav.meldeplikt.meldekortservice.config

import com.auth0.jwt.JWT
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.util.pipeline.PipelineContext
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais

fun PipelineContext<Unit, ApplicationCall>.extractIdentFromToken(): String {
    val authTokenHeader = getTokenFromHeader()
    val authTokenCookie = getTokenFromCookie()

    verifyThatIdentIsConsistent(authTokenHeader, authTokenCookie)

    val authToken = authTokenHeader ?: authTokenCookie

    verifyThatATokenWasFound(authToken)

    return extractSubject(authToken)
}

val PipelineContext<Unit, ApplicationCall>.userIdent get() = if (isCurrentlyRunningOnNais()) extractIdentFromToken() else "11111111111"

private fun verifyThatATokenWasFound(authToken: String?) {
    if (authToken == null) {
        val melding = "Token ble ikke funnet. Dette skal ikke kunne skje."
        defaultLog.error(melding)
        throw Exception(melding)
    }
}

private fun verifyThatIdentIsConsistent(authTokenHeader: String?, authTokenCookie: String?) {
    if (authTokenHeader != null && authTokenCookie != null) {
        val headerIdent = extractSubject(authTokenHeader)
        val cookieIdent = extractSubject(authTokenCookie)

        if (headerIdent != cookieIdent) {
            val melding = "Ident i header er ulik ident i cookie. Dette skal ikke kunne skje."
            defaultLog.error(melding)
            throw Exception(melding)
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getTokenFromHeader() =
    call.request.headers[HttpHeaders.Authorization]?.replace("Bearer ", "")

private fun PipelineContext<Unit, ApplicationCall>.getTokenFromCookie() =
    call.request.cookies["selvbetjening-idtoken"]

private fun extractSubject(authToken: String?): String {
    val jwt = JWT.decode(authToken)
    return jwt.getClaim("sub").asString() ?: "subject (ident) ikke funnet"
}