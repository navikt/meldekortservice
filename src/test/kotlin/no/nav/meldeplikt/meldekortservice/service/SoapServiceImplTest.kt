package no.nav.meldeplikt.meldekortservice.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SoapServiceImplTest {

    @Test
    fun `test pingWeblogic returns true`() {
        val oppfoelgingPing = mockk<SakOgAktivitetV1>()
        val soapServiceImpl = SoapServiceImpl(oppfoelgingPing = oppfoelgingPing)

        every { oppfoelgingPing.ping() } just Runs

        val result = soapServiceImpl.pingWeblogic()

        assertTrue(result.erWeblogicOppe)
    }

    @Test
    fun `test pingWeblogic returns false`() {
        val oppfoelgingPing = mockk<SakOgAktivitetV1>()
        val soapServiceImpl = SoapServiceImpl(oppfoelgingPing = oppfoelgingPing)

        every { oppfoelgingPing.ping() } throws RuntimeException("false")

        val result = soapServiceImpl.pingWeblogic()

        assertFalse( result.erWeblogicOppe)
    }
}
