package no.nav.meldeplikt.meldekortservice.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType

class HttpClient {



    val client: HttpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(DefaultRequest) {
            headers.append("Accept", "application/xml")
            headers.append("Authorization","Bearer ${hentToken()}")
        }
    }
}

fun hentToken(): String {
    cache.get("ordsToken", () -> hentOrdsToken())
}

fun hentOrdsToken(): String {
    return "token"
}

