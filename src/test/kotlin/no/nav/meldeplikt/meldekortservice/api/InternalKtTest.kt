package no.nav.meldeplikt.meldekortservice.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.locations.*
import no.nav.meldeplikt.meldekortservice.utils.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@KtorExperimentalLocationsAPI
class InternalKtTest : TestBase() {

    @Test
    fun testInternal() = setUpTestApplication {

        val response1 = client.get(INTERNAL_PATH)
        assertEquals(HttpStatusCode.OK, response1.status)
        assertEquals(SWAGGER_URL_V1, response1.request.url.encodedPath)

        val response2 = client.get("$INTERNAL_PATH/")
        assertEquals(HttpStatusCode.OK, response2.status)
        assertEquals(SWAGGER_URL_V1, response2.request.url.encodedPath)

        val response3 = client.get("$INTERNAL_PATH/apidocs")
        assertEquals(HttpStatusCode.OK, response3.status)
        assertEquals(SWAGGER_URL_V1, response3.request.url.encodedPath)

        val response4 = client.get("$INTERNAL_PATH/apidocs/")
        assertEquals(HttpStatusCode.OK, response4.status)
        assertEquals(SWAGGER_URL_V1, response4.request.url.encodedPath)

        val response5 = client.get("$INTERNAL_PATH/apidocs/swagger.json")
        assertEquals(HttpStatusCode.OK, response5.status)
        assertEquals(
            defaultObjectMapper.writeValueAsString(swagger).replace("\n", "").replace("\r", "").replace(" ", ""),
            response5.bodyAsText().replace("\n", "").replace("\r", "").replace(" ", "")
        )

        val response6 = client.get("$INTERNAL_PATH/isAlive")
        assertEquals(HttpStatusCode.OK, response6.status)
        assertEquals("Alive", response6.bodyAsText())

        val response7 = client.get("$INTERNAL_PATH/isReady")
        assertEquals(HttpStatusCode.OK, response7.status)
        assertEquals("Ready", response7.bodyAsText())

        val response8 = client.get("$INTERNAL_PATH/ping")
        assertEquals(HttpStatusCode.OK, response8.status)
        assertEquals("""{"ping": "pong"}""", response8.bodyAsText())

        val response9 = client.get("$INTERNAL_PATH/metrics")
        assertEquals(HttpStatusCode.OK, response9.status)

        val response10 = client.get(BASE_PATH)
        assertEquals(HttpStatusCode.OK, response10.status)
        assertEquals(SWAGGER_URL_V1, response10.request.url.encodedPath)

        val response11 = client.get("$BASE_PATH/")
        assertEquals(HttpStatusCode.OK, response11.status)
        assertEquals(SWAGGER_URL_V1, response11.request.url.encodedPath)

        val response12 = client.get("$BASE_PATH/api")
        assertEquals(HttpStatusCode.OK, response12.status)
        assertEquals(SWAGGER_URL_V1, response12.request.url.encodedPath)

        val response13 = client.get("$BASE_PATH/api/")
        assertEquals(HttpStatusCode.OK, response13.status)
        assertEquals(SWAGGER_URL_V1, response13.request.url.encodedPath)
    }
}
