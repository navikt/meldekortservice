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
import java.time.Instant
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

val IncomingCallDatabaseLoggingPlugin: ApplicationPlugin<IncomingCallDatabaseLoggingPluginConfig> =
    createApplicationPlugin("IncomingCallDatabaseLoggingPlugin", ::IncomingCallDatabaseLoggingPluginConfig) {

        val dbService: DBService = pluginConfig.dbs
        val mock: Boolean = pluginConfig.mock
        val korrelasjonIdAttr = AttributeKey<String>("korrelasjonId")

        onCall { call ->

            if (!call.request.path().startsWith(API_PATH)) {
                return@onCall
            }

            val korrelasjonId = call.callId ?: ""
            call.attributes.put(korrelasjonIdAttr, korrelasjonId)

            val request = StringBuilder().apply {
                val request = call.request

                appendLine("Received request:")
                appendLine("${request.httpMethod.value} ${request.host()}:${request.port()}${request.uri} ${request.httpVersion}")

                request.headers.forEach { header, values ->
                    appendLine("$header: ${values.joinToString(",", "[", "]")}")
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
                korrelasjonId,
                LocalDateTime.now(),
                "REST",
                "INN",
                call.request.httpMethod.value,
                call.request.uri,
                0,
                Instant.now().toEpochMilli(),
                request,
                null,
                ""
            )

            if (!mock) {
                dbService.lagreRequest(kallLogg)
            }
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
                    appendLine("$header: ${values.joinToString(",", "[", "]")}")
                }

                // empty line before body as in HTTP response
                appendLine()

                // body
                append(readBody(call.application.coroutineContext, content))

                // new line after body because in the log there might be additional info after "log message"
                // and we don't want it to be mixed with logged body
                appendLine()
            }.toString()

            if (!mock) {
                dbService.lagreResponse(korrelasjonId, call.response.status()?.value ?: 0, response)
            }
        }
    }

class IncomingCallDatabaseLoggingPluginConfig {
    lateinit var dbs: DBService
    var mock: Boolean = false
}

private fun readBody(callContext: CoroutineContext, subject: Any): String = when (subject) {
    is TextContent -> subject.text
    is OutputStreamContent -> {
        val channel = ByteChannel(true)
        runBlocking {
            GlobalScope.writer(callContext, autoFlush = true) {
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
