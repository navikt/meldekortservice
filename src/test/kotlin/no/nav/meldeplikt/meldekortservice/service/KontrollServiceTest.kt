package no.nav.meldeplikt.meldekortservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.HttpMethod
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.config.Environment
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.FravaerInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Meldekortkontroll
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.MeldeperiodeInn
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.Sporsmal
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.utils.objectMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class KontrollServiceTest {
    val environment = Environment(
        oauthClientId = "test",
        oauthJwk = "test",
        oauthClientSecret = "test",
        oauthEndpoint = "test",
        oauthTenant = "test",
        dbHostPostgreSQL = "jdbc:h2:mem:testdb",
        dbUrlPostgreSQL = "jdbc:h2:mem:testdb",
        dbUserPostgreSQL = "sa",
        dbPasswordPostgreSQL = ""
    )
    val mapper = jacksonObjectMapper()
    val fnr = "1111111111"
    @Test
    fun kontroller() {
        val objectMapper: ObjectMapper = ObjectMapper()
            //.registerKotlinModule()
        val kontrollResponse: KontrollResponse = KontrollResponse(meldekortId = 123, status = "test")
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
                    request.body.contentType.toString() shouldBe "application/json"
                    request.url.toString() shouldBe "https://dummyUrl.com/api/kontroll"
                    respondOk(
                        mapper.writeValueAsString(kontrollResponse)
                    )
                }
            }
           /* install(JsonFeature) {
                serializer = JacksonSerializer { objectMapper }
            }*/
        }
        val aadService: AadService = mockk<AadService>()
        coEvery { aadService.fetchAadToken() } returns "token"

        val kontrollService = KontrollService(
            env = environment,
            aadService = aadService,
            kontrollClient = client
        )

        runBlocking {
            var actualResponse = kontrollService.kontroller(meldekortkontroll)

        }
    }
}