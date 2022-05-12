package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.features.json.*
import io.ktor.http.HttpMethod
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.LocalDateDeserializer
import no.nav.meldeplikt.meldekortservice.utils.LocalDateSerializer
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.objectMapper
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class KontrollServiceTest {
    val fnr = "11111111111"
//    @Test
//    fun kontroller() {
//        val kontrollResponse: KontrollResponse = KontrollResponse(meldekortId = 123, status = "test")
//        val meldekortkontroll = Meldekortkontroll(
//            meldekortId = 123,
//            fnr = fnr,
//            personId = 335,
//            kilde = "MELDEPLIKT",
//            kortType = "ELEKTRONISK",
//            periodeFra = LocalDate.parse("2020-01-20", DateTimeFormatter.ISO_DATE),
//            periodeTil = LocalDate.parse("2020-02-02", DateTimeFormatter.ISO_DATE),
//            meldedato = LocalDate.parse("2020-02-03", DateTimeFormatter.ISO_DATE),
//            meldegruppe = "DAGP",
//            arbeidssoker = true,
//            arbeidet = false,
//            syk = false,
//            annetFravaer = true,
//            kurs = true,
//            begrunnelse = "Begrunnelse",
//            meldekortdager = listOf(FravaerInn(dato = LocalDate.parse("2020-01-21", DateTimeFormatter.ISO_DATE), syk=true, kurs=true, annetFravaer=true, arbeidTimer=0.0))
//        )
//        val xmlString = """<KopierMeldekortResponse><meldekortId>123</meldekortId></KopierMeldekortResponse>"""
//        val client = HttpClient(MockEngine) {
//            engine {
//                addHandler { request ->
//                    request.method shouldBe HttpMethod.Post
//                    request.headers["Authorization"] shouldNotBe null
//                    request.headers["Authorization"] shouldStartWith "Bearer token"
//                    //request.body.contentType.toString() shouldBe "application/json"
//                    request.url.toString() shouldBe "https://dummyurl.nav.no/api/v1/kontroll"
//                    respondOk(
//                        defaultObjectMapper.writeValueAsString(kontrollResponse)
//                    )
//                }
//            }
//            install(JsonFeature) {
//                serializer = JacksonSerializer { objectMapper }
//            }
//        }
//        val aadService: AadService = mockk<AadService>()
//        coEvery { aadService.fetchAadToken() } returns "token"
//
//        val kontrollService = KontrollService(
//            aadService = aadService,
//            kontrollClient = client
//        )
//
//        runBlocking {
//            var actualResponse = kontrollService.kontroller(meldekortkontroll)
//            assertEquals(actualResponse.meldekortId, kontrollResponse.meldekortId)
//            assertEquals(actualResponse.status, kontrollResponse.status)
//        }
//    }
}