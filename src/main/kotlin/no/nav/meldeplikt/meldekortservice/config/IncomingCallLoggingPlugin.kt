package no.nav.meldeplikt.meldekortservice.config

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.API_PATH
import no.nav.meldeplikt.meldekortservice.utils.generateCallId
import no.nav.meldeplikt.meldekortservice.utils.headersToString
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.CoroutineContext

val IncomingCallLoggingPlugin: ApplicationPlugin<ICDLPConfig> =
    createApplicationPlugin("IncomingCallLoggingPlugin", ::ICDLPConfig) {

        val dbService: DBService = pluginConfig.dbs
        val korrelasjonIdAttr = AttributeKey<String>("korrelasjonId")

        onCall { call ->

            if (!call.request.path().startsWith(API_PATH)) {
                return@onCall
            }

            currentCallId = call.callId ?: generateCallId()
            val korrelasjonId = currentCallId
            call.attributes.put(korrelasjonIdAttr, korrelasjonId)

            val request = StringBuilder().apply {
                val request = call.request

                appendLine("Received request:")
                appendLine("${request.httpMethod.value} ${request.host()}:${request.port()}${request.uri} ${request.httpVersion}")

                request.headers.forEach { header, values ->
                    appendLine("$header: ${headersToString(values)}")
                }

                // empty line before body as in HTTP request
                appendLine()

                // body
                append(call.receiveText())

                // new line after body because in the log there might be additional info after "log message"
                // and we don't want it to be mixed with logged body
                appendLine()
            }.toString()

            val kallLogg = KallLogg(
                korrelasjonId = korrelasjonId,
                tidspunkt = LocalDateTime.now(),
                type = "REST",
                kallRetning = "INN",
                method = call.request.httpMethod.value,
                operation = call.request.uri,
                status = 0,
                kallTid = Instant.now().toEpochMilli(),
                request = request,
                response = null,
                logginfo = ""
            )

            dbService.lagreRequest(kallLogg)
        }

        on(ResponseBodyReadyForSend) { call, content ->
            if (!call.request.path().startsWith(API_PATH)) {
                return@on
            }

            val korrelasjonId = call.attributes[korrelasjonIdAttr]

            val response = StringBuilder().apply {
                val response = call.response

                appendLine("Sent response:")
                appendLine("${response.status()?.value} ${response.status()?.description}")

                response.headers.allValues().forEach { header, values ->
                    appendLine("$header: ${headersToString(values)}")
                }

                // empty line before body as in HTTP response
                appendLine()

                // body
                append(readBody(call.application.coroutineContext, content))

                // new line after body because in the log there might be additional info after "log message"
                // and we don't want it to be mixed with logged body
                appendLine()
            }.toString()

            dbService.lagreResponse(korrelasjonId, call.response.status()?.value ?: 0, response)
        }
    }

class ICDLPConfig {
    lateinit var dbs: DBService
}

private fun readBody(coroutineContext: CoroutineContext, subject: Any): String = when (subject) {
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
