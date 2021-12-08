package no.nav.meldeplikt.meldekortservice.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpMethod
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KontrollServiceTest {
    val fnr = "1111111111"
    //TODO trenger å fikse feil
    @Test
    fun kontroller() {
       /* val kontrollResponse: KontrollResponse = KontrollResponse(meldekortId = 123, status = "test")
        val meldekortkontroll = Meldekortkontroll(
            meldekortId = 123,
            personId = 335,
            kortType = "MASKINELT_OPPDATERT",
            kortStatus = "SENDT",
            meldegruppe = "DAGP",
            meldeperiode = MeldeperiodeInn(fra =  LocalDate.parse("2020-01-20", DateTimeFormatter.ISO_DATE), til =  LocalDate.parse("2020-02-02", DateTimeFormatter.ISO_DATE), kortKanSendesFra = LocalDate.parse("2020-02-01", DateTimeFormatter.ISO_DATE), kanKortSendes = true, periodeKode = "202004"),
            fravaersdager = listOf(FravaerInn(dag =  LocalDate.parse("2020-01-21", DateTimeFormatter.ISO_DATE), harSyk=true, harKurs=true, harAnnet=true, arbeidTimer=0.0)),
            sporsmal = Sporsmal(arbeidssoker=true, arbeidet=false, syk=false, annetFravaer=true, kurs=true, forskudd=false, signatur=false),
            begrunnelse = "Begrunnelse"
        )
        val xmlString = """<KopierMeldekortResponse><meldekortId>123</meldekortId></KopierMeldekortResponse>"""
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    request.method shouldBe HttpMethod.Post
                    request.headers["Authorization"] shouldNotBe null
                    request.headers["Authorization"] shouldStartWith "Bearer token"
                    //request.body.contentType.toString() shouldBe "application/json"
                    request.url.toString() shouldBe "https://dummyurl.nav.no/api/kontroll"
                    respondOk(
                        defaultObjectMapper.writeValueAsString(kontrollResponse)
                    )
                }
            }
           *//* install(JsonFeature) {
                serializer = JacksonSerializer { objectMapper }
            }*//*
        }
        val aadService: AadService = mockk<AadService>()
        coEvery { aadService.fetchAadToken() } returns "token"

        val kontrollService = KontrollService(
            aadService = aadService,
            kontrollClient = client
        )

        runBlocking {
            var actualResponse = kontrollService.kontroller(meldekortkontroll)

        }*/
    }
}