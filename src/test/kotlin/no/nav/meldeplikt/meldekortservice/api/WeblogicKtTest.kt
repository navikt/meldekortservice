package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.locations.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.service.SoapServiceImpl
import no.nav.meldeplikt.meldekortservice.utils.defaultObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@KtorExperimentalLocationsAPI
class WeblogicKtTest : TestBase() {
    private val soapServiceImpl = mockk<SoapServiceImpl>()

    @Test
    fun `test weblogic returns true when Arena is up`() = setUpTestApplication {

        mockkObject(SoapConfig)
        every { SoapConfig.soapService() } returns soapServiceImpl
        every { soapServiceImpl.pingWeblogic() } returns WeblogicPing(erWeblogicOppe = true)

        val response = client.get("/meldekortservice/api/weblogic")
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<WeblogicPing>(response.bodyAsText())
        assertTrue(responseObject.erWeblogicOppe)
    }

    @Test
    fun `test weblogic returns false when Arena is not up`() = setUpTestApplication {

        mockkObject(SoapConfig)
        every { SoapConfig.soapService() } returns soapServiceImpl
        every { soapServiceImpl.pingWeblogic() } returns WeblogicPing(erWeblogicOppe = false)

        val response = client.get("/meldekortservice/api/weblogic")
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.bodyAsText())
        val responseObject = defaultObjectMapper.readValue<WeblogicPing>(response.bodyAsText())
        assertFalse(responseObject.erWeblogicOppe)
    }
}
