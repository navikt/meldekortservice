package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.config.MapApplicationConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import io.mockk.*
import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekort.FravaerType
import no.nav.meldeplikt.meldekortservice.model.meldekort.Meldekort
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.MeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.meldeplikt.meldekortservice.model.response.OrdsStringResponse
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.InnsendtMeldekortService
import no.nav.meldeplikt.meldekortservice.service.KontrollService
import no.nav.meldeplikt.meldekortservice.service.SoapServiceImpl
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.amshove.kluent.shouldBe
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
class PersonKtTest {
    private fun MapApplicationConfig.setOidcConfig() {
        put("no.nav.security.jwt.issuers.size", "1")
        put("no.nav.security.jwt.issuers.0.issuer_name", ISSUER_ID)
        put("no.nav.security.jwt.issuers.0.discoveryurl", mockOAuth2Server.wellKnownUrl(ISSUER_ID).toString())
        put("no.nav.security.jwt.issuers.0.accepted_audience", REQUIRED_AUDIENCE)
        put("no.nav.security.jwt.required_issuer_name", ISSUER_ID)
        put("ktor.environment", "local")
    }

    private fun issueToken(): String =
        mockOAuth2Server.issueToken(
            ISSUER_ID,
            "myclient",
            DefaultOAuth2TokenCallback(audience = listOf(REQUIRED_AUDIENCE), claims = mapOf("sub" to "11111111111"))
            ).serialize()

    companion object {
        private const val ISSUER_ID = "default"
        private const val REQUIRED_AUDIENCE = "default"

        private val mockOAuth2Server = MockOAuth2Server()
        private val flywayConfig = mockk<Flyway>()
        private val arenaOrdsService = mockk<ArenaOrdsService>()
        private val innsendtMeldekortService = mockk<InnsendtMeldekortService>()
        private val kontrollService = mockk<KontrollService>()

        @BeforeAll
        @JvmStatic
        fun setup() {
            mockOAuth2Server.start(8091)
            every { flywayConfig.migrate() } returns 0
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            mockOAuth2Server.shutdown()
        }
    }

    @Test
    fun `get historiske meldekort returns ok with valid JWT`() {
        val period = 1
        val fnr = "11111111111"
        val person = Person(1L, "Bob", "Kåre", "No", "Papp", listOf<Meldekort>(), 10, listOf<FravaerType>())

        coEvery { arenaOrdsService.hentHistoriskeMeldekort(fnr, period) } returns (person)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                    kontrollService = mockk(),
                    mockInnsendtMeldekortService = mockk(),
                    mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/person/historiskemeldekort?antallMeldeperioder=${period}") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")
            }.apply {
                val mapper = jacksonObjectMapper()
                assertNotNull(response.content)
                val responseObject = mapper.readValue<Person>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                assertEquals(person.personId, responseObject.personId)
            }
        }
    }

    @Test
    fun `get historiske meldekort returns 401-Unauthorized with missing JWT`() {
        val period = 1
        val fnr = "11111111111"
        val person = Person(1L, "Bob", "Kåre", "No", "Papp", listOf<Meldekort>(), 10, listOf<FravaerType>())

        coEvery { arenaOrdsService.hentHistoriskeMeldekort(fnr, period) } returns (person)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                    kontrollService = mockk(),
                    mockInnsendtMeldekortService = mockk(),
                    mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/person/historiskemeldekort?antallMeldeperioder=${period}") {
            }.apply {
                response.status() shouldBe HttpStatusCode.Unauthorized
            }
        }
    }

    @Test
    fun `get person meldekort returns ok with valid JWT`() {
        val mapper = XmlMapper().registerModule(KotlinModule())
        val jsonMapper = jacksonObjectMapper()
        val meldekort1 = Meldekort(
                1L,
                KortType.MASKINELT_OPPDATERT.code,
                "201920",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "DAGP",
                "Ferdig",
                false,
                LocalDate.now().minusDays(1),
                3F
        )
        val meldekort2 = Meldekort(
                2L,
                KortType.MASKINELT_OPPDATERT.code,
                "201920",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "DAGP",
                "Ferdig",
                false,
                LocalDate.now().minusDays(1),
                3F
        )
        val person = Person(1L, "Bob", "Kåre", "No", "Papp", listOf<Meldekort>(meldekort1, meldekort2), 10, listOf<FravaerType>())
        val ordsStringResponse = OrdsStringResponse(status = HttpStatusCode.OK, content = mapper.writeValueAsString(person))

        coEvery { arenaOrdsService.hentMeldekort(any()) } returns (ordsStringResponse)
        coEvery { innsendtMeldekortService.hentInnsendtMeldekort(1L) } returns (InnsendtMeldekort(meldekortId = 1L))
        coEvery { innsendtMeldekortService.hentInnsendtMeldekort(2L) } throws SQLException("Found no rows")

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                    kontrollService = mockk(),
                    mockInnsendtMeldekortService = innsendtMeldekortService,
                    mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/person/meldekort") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")
            }.apply {
                assertNotNull(response.content)
                val responseObject = jsonMapper.readValue<Person>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                assertEquals(person.personId, responseObject.personId)
                assertEquals(1, responseObject.meldekortListe?.size)
            }
        }
    }

    @Test
    fun `get person meldekort returns NoContent status when no response from ORDS`() {
        val ordsStringResponse = OrdsStringResponse(status = HttpStatusCode.BadRequest, content = "")

        coEvery { arenaOrdsService.hentMeldekort(any()) } returns (ordsStringResponse)

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                    kontrollService = mockk(),
                    mockInnsendtMeldekortService = innsendtMeldekortService,
                    mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/person/meldekort") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")
            }.apply {
                response.status() shouldBe HttpStatusCode.NoContent
            }
        }
    }

    @Test
    fun `Kontroll or innsending of meldekort returns OK`() {
        val meldekortdetaljer = Meldekortdetaljer(id = "1",
                fodselsnr = "11111111111",
                kortType = KortType.AAP,
                meldeperiode = "20200105",
                sporsmal = Sporsmal(meldekortDager = listOf())
        )
        val jsonMapper = jacksonObjectMapper()
        val meldekortKontrollertType = MeldekortKontrollertType()
        meldekortKontrollertType.meldekortId = 1L
        meldekortKontrollertType.status = "OK"
        meldekortKontrollertType.arsakskoder = MeldekortKontrollertType.Arsakskoder()


        mockkObject(SoapConfig)
        val externControlEmeldingSOAP = mockk<ExternControlEmeldingSOAP>()

        every { SoapConfig.soapService() } returns SoapServiceImpl(externControlEmeldingSOAP, mockk())
        every { externControlEmeldingSOAP.kontrollerEmeldingMeldekort(any()) } returns meldekortKontrollertType

        coEvery { innsendtMeldekortService.settInnInnsendtMeldekort(any()) } just Runs
        coEvery { kontrollService.kontroller(any()) } returns meldekortKontrollertType
        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                    kontrollService = kontrollService,
                    mockInnsendtMeldekortService = innsendtMeldekortService,
                    mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Post, "/meldekortservice/api/person/meldekort") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(ObjectMapper().writeValueAsString(meldekortdetaljer))
            }.apply {
                assertNotNull(response.content)
                val responseObject = jsonMapper.readValue<MeldekortKontrollertType>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                assertEquals(meldekortKontrollertType.meldekortId, responseObject.meldekortId)
            }
        }
    }

    @Test
    fun `Kontroll of meldekort returns ServiceUnavailable`() {
        val meldekortdetaljer = Meldekortdetaljer(id = "1",
                fodselsnr = "11111111111",
                kortType = KortType.AAP,
                meldeperiode = "20200105",
                sporsmal = Sporsmal(meldekortDager = listOf<MeldekortDag>())
        )
        val meldekortKontrollertType = MeldekortKontrollertType()
        meldekortKontrollertType.meldekortId = 1L
        meldekortKontrollertType.status = "OK"
        meldekortKontrollertType.arsakskoder = MeldekortKontrollertType.Arsakskoder()


        mockkObject(SoapConfig)
        val externControlEmeldingSOAP = mockk<ExternControlEmeldingSOAP>()

        every { SoapConfig.soapService() } returns SoapServiceImpl(externControlEmeldingSOAP, mockk())
        every { externControlEmeldingSOAP.kontrollerEmeldingMeldekort(any()) } throws RuntimeException("Error i arena")

        coEvery { innsendtMeldekortService.settInnInnsendtMeldekort(any()) } just Runs
        coEvery { kontrollService.kontroller(any()) } returns meldekortKontrollertType
        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns true

        withTestApplication({
            (environment.config as MapApplicationConfig).setOidcConfig()
            mainModule(arenaOrdsService  = arenaOrdsService,
                    kontrollService = kontrollService,
                    mockInnsendtMeldekortService = innsendtMeldekortService,
                    mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Post, "/meldekortservice/api/person/meldekort") {
                addHeader(HttpHeaders.Authorization, "Bearer ${issueToken()}")
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(ObjectMapper().writeValueAsString(meldekortdetaljer))
            }.apply {
                response.status() shouldBe HttpStatusCode.ServiceUnavailable
            }
        }
    }
}