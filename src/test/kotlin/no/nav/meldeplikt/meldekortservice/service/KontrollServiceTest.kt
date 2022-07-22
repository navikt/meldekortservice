package no.nav.meldeplikt.meldekortservice.service

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollFeil
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.objectMapper
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class KontrollServiceTest {

    @Test
    fun kontrollerSkalReturnereOk() {
        val kontrollResponse = KontrollResponse(meldekortId = 123, kontrollStatus = "OK")

        test(kontrollResponse, "OK")
    }

    @Test
    fun kontrollerSkalReturnereFeil() {
        val kontrollResponse = KontrollResponse(
            meldekortId = 123, kontrollStatus = "OK", feilListe = listOf(
                KontrollFeil(kode = "", tekst = "", dag = 1)
            )
        )

        test(kontrollResponse, "FEIL")
    }

    private fun test(kontrollResponse: KontrollResponse, forventetStatus: String) {
        val meldekortkontroll = Meldekortkontroll(
            meldekortId = 123,
            fnr = "11111111111",
            personId = 335,
            kilde = "MELDEPLIKT",
            kortType = "ELEKTRONISK",
            periodeFra = LocalDate.parse("2020-01-20", DateTimeFormatter.ISO_DATE),
            periodeTil = LocalDate.parse("2020-02-02", DateTimeFormatter.ISO_DATE),
            meldedato = LocalDate.parse("2020-02-03", DateTimeFormatter.ISO_DATE),
            meldegruppe = "DAGP",
            arbeidssoker = true,
            arbeidet = false,
            syk = false,
            annetFravaer = true,
            kurs = false,
            begrunnelse = "Begrunnelsen er fin",
            meldekortdager = listOf(
                FravaerInn(
                    dato = LocalDate.parse("2020-01-21", DateTimeFormatter.ISO_DATE),
                    syk = false,
                    kurs = false,
                    annetFravaer = true,
                    arbeidTimer = 0.0
                )
            )
        )

        val client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                register(
                    ContentType.Application.Json,
                    JacksonConverter(objectMapper)
                )
            }

            engine {
                addHandler { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("Bearer token", request.headers["Authorization"])
                    assertEquals(ContentType.Application.Json, request.body.contentType)
                    assertEquals("https://dummyurl.nav.no/api/v1/kontroll", request.url.toString())
                    respond(
                        defaultObjectMapper.writeValueAsString(kontrollResponse),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }

        val aadService: AadService = mockk()
        coEvery { aadService.fetchAadToken() } returns "token"

        val kontrollService = KontrollService(
            aadService = aadService,
            kontrollClient = client
        )

        runBlocking {
            val actualResponse = kontrollService.kontroller(meldekortkontroll)
            assertEquals(kontrollResponse.meldekortId, actualResponse.meldekortId)
            assertEquals(forventetStatus, actualResponse.status)
        }
    }

}
