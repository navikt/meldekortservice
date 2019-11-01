package no.nav.meldeplikt.meldekortservice.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import no.nav.meldeplikt.meldekortservice.utils.objectMapper

class HttpClient {
    val client: HttpClient = HttpClient(Apache) {
        engine {
            response.apply {
                charset(Charsets.UTF_8.displayName())
            }
        }
        install(JsonFeature) {
            serializer = JacksonSerializer() { objectMapper }
        }
    }
}

