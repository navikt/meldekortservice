package no.nav.meldeplikt.meldekortservice.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.aetat.amelding.externcontrolemelding.webservices.ExternControlEmeldingSOAP
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.MeldekortDag
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SoapServiceImplTest {
    @Test
    fun `test kontrollerMeldekort returns MeldekortKontrollertType`() {
        val amelding = mockk<ExternControlEmeldingSOAP>()
        val soapServiceImpl = SoapServiceImpl(amelding = amelding, oppfoelgingPing = mockk())
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

        every { amelding.kontrollerEmeldingMeldekort(any()) } returns meldekortKontrollertType

        val result = soapServiceImpl.kontrollerMeldekort(meldekortdetaljer)

        assertEquals(meldekortKontrollertType.meldekortId, result.meldekortId)
    }

    @Test
    fun `test pingWeblogic returns true`() {
        val oppfoelgingPing = mockk<SakOgAktivitetV1>()
        val soapServiceImpl = SoapServiceImpl(amelding = mockk(), oppfoelgingPing = oppfoelgingPing)

        every { oppfoelgingPing.ping() } just Runs

        val result = soapServiceImpl.pingWeblogic()

        assertTrue(result.erWeblogicOppe)
    }

    @Test
    fun `test pingWeblogic returns false`() {
        val oppfoelgingPing = mockk<SakOgAktivitetV1>()
        val soapServiceImpl = SoapServiceImpl(amelding = mockk(), oppfoelgingPing = oppfoelgingPing)

        every { oppfoelgingPing.ping() } throws RuntimeException("false")

        val result = soapServiceImpl.pingWeblogic()

        assertFalse( result.erWeblogicOppe)
    }
}