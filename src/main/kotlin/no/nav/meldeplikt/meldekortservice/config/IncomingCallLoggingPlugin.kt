package no.nav.meldeplikt.meldekortservice.config

import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutputStreamContent
import io.ktor.http.content.TextContent
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.ResponseBodyReadyForSend
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.request.*
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.readUTF8LineTo
import io.ktor.utils.io.writer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.*
import java.time.Instant
import java.time.LocalDateTime

val IncomingCallLoggingPlugin: ApplicationPlugin<ICDLPConfig> =
    createApplicationPlugin("IncomingCallLoggingPlugin", ::ICDLPConfig) {

        val dbService: DBService = pluginConfig.dbs
        val kallLoggIdAttr = AttributeKey<Long>("kallLoggId")
        var body = ""

        onCall { call ->

            if (!call.request.path().startsWith(API_PATH)) {
                return@onCall
            }

            val token = call.request.headers[HttpHeaders.Authorization]?.replace("Bearer ", "")
            val fnr = extractSubject(token)

            val requestData = StringBuilder().apply {
                val request = call.request

                appendLine("${request.httpMethod.value} ${request.host()}:${request.port()}${request.uri} ${request.httpVersion}")

                request.headers.forEach { header, values ->
                    appendLine("$header: ${headersToString(values)}")
                }

                // empty line before body as in HTTP request
                appendLine()

                // body
                appendLine(call.receiveText())
            }.toString()

            try {
                val kallLoggId = dbService.lagreKallLogg(
                    KallLogg(
                        korrelasjonId = getCallId(),
                        tidspunkt = LocalDateTime.now(),
                        type = "REST",
                        kallRetning = "INN",
                        method = call.request.httpMethod.value,
                        operation = call.request.path(),
                        status = 0,
                        kallTid = Instant.now().toEpochMilli(),
                        request = requestData,
                        response = "",
                        logginfo = fnr
                    )
                )
                call.attributes.put(kallLoggIdAttr, kallLoggId)
            } catch (e: Exception) {
                defaultLog.error("Kunne ikke lagre kall logg", e)
            }
        }

        on(ResponseBodyReadyForSend) { call, content ->
            if (!call.request.path().startsWith(API_PATH)) {
                return@on
            }

            body = readBody(content)
        }

        on(ResponseSent) { call ->
            if (!call.request.path().startsWith(API_PATH)) {
                return@on
            }

            val kallLoggId = call.attributes[kallLoggIdAttr]

            val responseData = StringBuilder().apply {
                val response = call.response

                appendLine("${response.status()?.value} ${response.status()?.description}")

                response.headers.allValues().forEach { header, values ->
                    appendLine("$header: ${headersToString(values)}")
                }

                // empty line before body as in HTTP response
                appendLine()

                // body
                appendLine(body)
            }.toString()

            dbService.lagreResponse(kallLoggId, call.response.status()?.value ?: 0, responseData)
        }
    }

class ICDLPConfig {
    lateinit var dbs: DBService
}

private fun readBody(subject: Any): String = when (subject) {
    is TextContent -> subject.text
    is OutputStreamContent -> {
        val channel = ByteChannel(true)
        runBlocking {
            GlobalScope.writer(coroutineContext, autoFlush = true) {
                subject.writeTo(channel)
            }
            val buffer = StringBuilder()
            while (!channel.isClosedForRead) {
                channel.readUTF8LineTo(buffer)
            }
            buffer.toString()
        }
    }
    else -> String()
}
