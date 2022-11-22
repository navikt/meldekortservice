package no.nav.meldeplikt.meldekortservice.config

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.getCallId
import no.nav.meldeplikt.meldekortservice.utils.headersToString
import java.time.Instant
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

class OutgoingCallLoggingPlugin {

    fun intercept(httpClient: HttpClient) {
        httpClient.plugin(HttpSend).intercept { requestBuilder ->
            // Prepare
            val callId = getCallId()
            val startTime = LocalDateTime.now()
            val kallTid = Instant.now().toEpochMilli()

            requestBuilder.headers.append(HttpHeaders.XRequestId, callId)

            // Execute call
            val originalCall = execute(requestBuilder)

            // Save data
            val request = originalCall.request
            val response = originalCall.response

            val responseBody = response.bodyAsText(Charsets.UTF_8)

            try {
                defaultDbService.lagreKallLogg(
                    KallLogg(
                        korrelasjonId = callId,
                        tidspunkt = startTime,
                        type = "REST",
                        kallRetning = "UT",
                        method = request.method.value,
                        operation = request.url.encodedPath,
                        status = response.status.value,
                        kallTid = Instant.now().toEpochMilli() - kallTid,
                        request = buildRequest(requestBuilder.executionContext, request),
                        response = buildResponse(response, responseBody),
                        logginfo = ""
                    )
                )
            } catch (e: Exception) {
                defaultLog.error("Kunne ikke lagre kall logg", e)
            }

            // Response content can be read only once. Wrap the call with the content we have read
            originalCall.wrapWithContent(ByteReadChannel(responseBody.toByteArray()))
        }
    }

    private fun buildRequest(coroutineContext: CoroutineContext, request: HttpRequest): String {
        return StringBuilder().apply {
            appendLine("${request.method.value} ${request.url.protocol.name}://${request.url.hostWithPort}${request.url.fullPath}")

            request.headers.forEach { header, values ->
                appendLine("$header: ${headersToString(values)}")
            }

            // empty line before body as in HTTP request
            appendLine()

            when (request.content) {
                is OutgoingContent.ByteArrayContent -> {
                    append(
                        String(
                            (request.content as OutgoingContent.ByteArrayContent).bytes(),
                            Charsets.UTF_8
                        )
                    )
                }
                is OutgoingContent.WriteChannelContent -> {
                    val buffer = StringBuilder()
                    val channel = ByteChannel(true)

                    runBlocking {
                        GlobalScope.writer(coroutineContext, autoFlush = true) {
                            (request.content as OutgoingContent.WriteChannelContent).writeTo(channel)
                        }

                        while (!channel.isClosedForRead) {
                            channel.readUTF8LineTo(buffer)
                        }
                    }

                    appendLine(buffer.toString())
                }
                else -> {
                    appendLine(request.content)
                }
            }

        }.toString()
    }

    private fun buildResponse(response: HttpResponse, responseBody: String): String {
        return StringBuilder().apply {
            appendLine("${response.version} ${response.status.value} ${response.status.description}")

            response.headers.forEach { header, values ->
                appendLine("$header: ${headersToString(values)}")
            }

            // empty line before body as in HTTP response
            appendLine()

            appendLine(responseBody)
        }.toString()
    }
}
