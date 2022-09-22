package no.nav.meldeplikt.meldekortservice.config

import com.auth0.jwt.JWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import no.nav.meldeplikt.meldekortservice.utils.StaticVars.Companion.currentCallId
import no.nav.meldeplikt.meldekortservice.utils.defaultLog

fun PipelineContext<Unit, ApplicationCall>.extractIdentFromToken(): String {
    val authTokenHeader = getTokenFromHeader()
    val authTokenCookie = getTokenFromCookie()

    verifyThatIdentIsConsistent(authTokenHeader, authTokenCookie)

    val authToken = authTokenHeader ?: authTokenCookie

    verifyThatATokenWasFound(authToken)

    return extractSubject(authToken)
}

val PipelineContext<Unit, ApplicationCall>.userIdent get() = extractIdentFromToken()

private fun verifyThatATokenWasFound(authToken: String?) {
    if (authToken == null) {
        val melding = "Token ble ikke funnet. Dette skal ikke kunne skje. callId = $currentCallId"
        defaultLog.error(melding)
        throw Exception(melding)
    }
}

private fun verifyThatIdentIsConsistent(authTokenHeader: String?, authTokenCookie: String?) {
    if (authTokenHeader != null && authTokenCookie != null) {
        val headerIdent = extractSubject(authTokenHeader)
        val cookieIdent = extractSubject(authTokenCookie)

        if (headerIdent != cookieIdent) {
            val melding = "Ident i header er ulik ident i cookie. Dette skal ikke kunne skje. callId = $currentCallId"
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

    val pid = jwt.getClaim("pid")
    val sub = jwt.getClaim("sub")

    if (!pid.isNull) {
        return pid.asString()
    } else if (!sub.isNull) {
        return sub.asString()
    }

    return "subject (ident) ikke funnet"
}
