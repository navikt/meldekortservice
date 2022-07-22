package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.locations.*
import io.ktor.server.testing.*
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
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@KtorExperimentalLocationsAPI
class WeblogicKtTest {
    private val flywayConfig = mockk<Flyway>()

    @Test
    fun `test weblogic returns true when Arena is up`() = testApplication {
        val soapServiceImpl = mockk<SoapServiceImpl>()

        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false

        mockkObject(SoapConfig)
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { SoapConfig.soapService() } returns soapServiceImpl
        every { soapServiceImpl.pingWeblogic() } returns WeblogicPing(erWeblogicOppe = true)

        environment {
            config = ApplicationConfig("application-dummy.conf")
        }
        application {
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }

        val response = client.get("/meldekortservice/api/weblogic")
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<WeblogicPing>(response.bodyAsText())
        assertTrue(responseObject.erWeblogicOppe)
    }

    @Test
    fun `test weblogic returns false when Arena is not up`() = testApplication {
        val soapServiceImpl = mockk<SoapServiceImpl>()

        mockkObject(SoapConfig)
        mockkStatic(::isCurrentlyRunningOnNais)
        every { isCurrentlyRunningOnNais() } returns false
        every { flywayConfig.migrate() } returns MigrateResult("", "", "")
        every { SoapConfig.soapService() } returns soapServiceImpl
        every { soapServiceImpl.pingWeblogic() } returns WeblogicPing(erWeblogicOppe = false)

        environment {
            config = ApplicationConfig("application-dummy.conf")
        }
        application {
            mainModule(
                arenaOrdsService = mockk(),
                kontrollService = mockk(),
                mockDBService = mockk(),
                mockFlywayConfig = flywayConfig
            )
        }

        val response = client.get("/meldekortservice/api/weblogic")
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<WeblogicPing>(response.bodyAsText())
        assertFalse(responseObject.erWeblogicOppe)
    }
}
