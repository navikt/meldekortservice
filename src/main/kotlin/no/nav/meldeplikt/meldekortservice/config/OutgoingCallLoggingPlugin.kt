package no.nav.meldeplikt.meldekortservice.config

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.generateCallId
import no.nav.meldeplikt.meldekortservice.utils.getCallId
import no.nav.meldeplikt.meldekortservice.utils.headersToString
import java.time.Instant
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

class OutgoingCallLoggingPlugin(config: OCDLPConfig) {

    val dbService: DBService = config.dbs
    val kallLoggIdAttr = AttributeKey<Long>("kallLoggId")

    class OCDLPConfig {
        lateinit var dbs: DBService
    }

    companion object Plugin : HttpClientPlugin<OCDLPConfig, OutgoingCallLoggingPlugin> {
        override val key: AttributeKey<OutgoingCallLoggingPlugin> = AttributeKey("OutgoingCallLoggingPlugin")

        override fun prepare(block: OCDLPConfig.() -> Unit): OutgoingCallLoggingPlugin {
            val config = OCDLPConfig().apply(block)

            return OutgoingCallLoggingPlugin(config)
        }

        override fun install(plugin: OutgoingCallLoggingPlugin, scope: HttpClient) {
            /*
                scope.requestPipeline.intercept(HttpRequestPipeline.Before)
                scope.requestPipeline.intercept(HttpRequestPipeline.State)
                scope.requestPipeline.intercept(HttpRequestPipeline.Transform)
                scope.requestPipeline.intercept(HttpRequestPipeline.Render)
                scope.sendPipeline.intercept(HttpSendPipeline.Before)
                scope.sendPipeline.intercept(HttpSendPipeline.State)
                scope.sendPipeline.intercept(HttpSendPipeline.Monitoring)
                scope.sendPipeline.intercept(HttpSendPipeline.Engine)
                scope.receivePipeline.intercept(HttpReceivePipeline.Before)
                scope.receivePipeline.intercept(HttpReceivePipeline.State)
                scope.receivePipeline.intercept(HttpReceivePipeline.After)
                scope.sendPipeline.intercept(HttpSendPipeline.Receive)
                scope.requestPipeline.intercept(HttpRequestPipeline.Send)
                scope.responsePipeline.intercept(HttpResponsePipeline.Receive)
                scope.responsePipeline.intercept(HttpResponsePipeline.Parse)
                scope.responsePipeline.intercept(HttpResponsePipeline.Transform)
                scope.responsePipeline.intercept(HttpResponsePipeline.State)
                */

            var startTime = LocalDateTime.now()
            var kallTid = Instant.now().toEpochMilli()
            var responseBody = ""

            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                // Det er mulig at vi sender request før vi får noe request fra meldekort-api
                // F.eks for å hente noe config eller lignende
                // Det betyr at vi kkke har noe callId ennå og da må vi generere den
                val callId = getCallId() ?: generateCallId()
                startTime = LocalDateTime.now()
                kallTid = Instant.now().toEpochMilli()
                context.headers.append(HttpHeaders.XRequestId, callId)
            }

            scope.responsePipeline.intercept(HttpResponsePipeline.Receive) { (type, content) ->
                if (content !is ByteReadChannel) return@intercept

                val byteArray = ByteArray(content.availableForRead)
                content.readAvailable(byteArray)
                val result = ByteReadChannel(byteArray)
                val responseContainer = HttpResponseContainer(type, result)
                responseBody = String(byteArray, context.response.charset() ?: Charsets.UTF_8)

                proceedWith(responseContainer)
            }

            scope.responsePipeline.intercept(HttpResponsePipeline.After) {
                val callId = getCallId() ?: generateCallId()
                val request = context.request
                val response = context.response

                try {
                    val kallLoggId = plugin.dbService.lagreKallLogg(
                        KallLogg(
                            korrelasjonId = callId,
                            tidspunkt = startTime,
                            type = "REST",
                            kallRetning = "UT",
                            method = request.method.value,
                            operation = request.url.encodedPath,
                            status = response.status.value,
                            kallTid = Instant.now().toEpochMilli() - kallTid,
                            request = buildRequest(context.coroutineContext, request),
                            response = buildResponse(response, responseBody),
                            logginfo = ""
                        )
                    )
                    response.call.attributes.put(plugin.kallLoggIdAttr, kallLoggId)
                } catch (e: Exception) {
                    defaultLog.error("Kunne ikke llagre kall logg", e)
                }
            }
        }

        private fun buildRequest(coroutineContext: CoroutineContext, request: HttpRequest): String {
            return StringBuilder().apply {
                appendLine("Sent request:")
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
                appendLine("Received response:")
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
}