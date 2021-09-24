package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.config.mainModule
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.service.SoapServiceImpl
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import org.amshove.kluent.shouldBe
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
class WeblogicKtTest {
    private val flywayConfig = mockk<Flyway>()

    @Test
    fun `test weblogic returns true when Arena is up`() {
        val soapServiceImpl = mockk<SoapServiceImpl>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false

        mockkObject(SoapConfig)
        every { flywayConfig.migrate() } returns 0
        every { SoapConfig.soapService() } returns soapServiceImpl
        every { soapServiceImpl.pingWeblogic() } returns WeblogicPing(erWeblogicOppe = true)

        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/weblogic") {
            }.apply {
                kotlin.test.assertNotNull(response.content)
                val responseObject = defaultObjectMapper.readValue<WeblogicPing>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                kotlin.test.assertTrue(responseObject.erWeblogicOppe)
            }
        }
    }

    @Test
    fun `test weblogic returns false when Arena is not up`() {
        val soapServiceImpl = mockk<SoapServiceImpl>()

        mockkObject(SoapConfig)
        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns 0
        every { SoapConfig.soapService() } returns soapServiceImpl
        every { soapServiceImpl.pingWeblogic() } returns WeblogicPing(erWeblogicOppe = false)

        withTestApplication({
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }) {
            handleRequest(HttpMethod.Get, "/meldekortservice/api/weblogic") {
            }.apply {
                kotlin.test.assertNotNull(response.content)
                val responseObject = defaultObjectMapper.readValue<WeblogicPing>(response.content!!)
                response.status() shouldBe HttpStatusCode.OK
                kotlin.test.assertFalse(responseObject.erWeblogicOppe)
            }
        }
    }
}