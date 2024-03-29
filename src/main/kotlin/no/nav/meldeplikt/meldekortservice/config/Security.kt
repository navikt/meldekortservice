package no.nav.meldeplikt.meldekortservice.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.extractSubject
import no.nav.meldeplikt.meldekortservice.utils.getCallId

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
        val melding = "Token ble ikke funnet. Dette skal ikke kunne skje. callId = ${getCallId()}"
        defaultLog.error(melding)
        throw Exception(melding)
    }
}

private fun verifyThatIdentIsConsistent(authTokenHeader: String?, authTokenCookie: String?) {
    if (authTokenHeader != null && authTokenCookie != null) {
        val headerIdent = extractSubject(authTokenHeader)
        val cookieIdent = extractSubject(authTokenCookie)

        if (headerIdent != cookieIdent) {
            val melding = "Ident i header er ulik ident i cookie. Dette skal ikke kunne skje. callId = ${getCallId()}"
            defaultLog.error(melding)
            throw Exception(melding)
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getTokenFromHeader() =
    call.request.headers[HttpHeaders.Authorization]?.replace("Bearer ", "")

private fun PipelineContext<Unit, ApplicationCall>.getTokenFromCookie() =
    call.request.cookies["selvbetjening-idtoken"]
