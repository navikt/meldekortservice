package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.enum.KortType
import no.nav.meldeplikt.meldekortservice.model.meldekort.FravaerType
import no.nav.meldeplikt.meldekortservice.model.meldekort.Meldekort
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.service.InnsendtMeldekortService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.sql.SQLException
import java.time.LocalDate

class MeldekortMapperTest {

    @Test
    fun testMeldekortMapper() {
        val mockMeldekortService = mock(InnsendtMeldekortService::class.java)
        `when`(mockMeldekortService.hentInnsendtMeldekort(2L)).thenAnswer { throw SQLException("Found no rows") }
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
        val meldekortListe = listOf(meldekort1, meldekort2)
        val fravaerListe = listOf<FravaerType>()
        val person = Person(1L, "Bob", "Kåre", "No", "Papp", meldekortListe, 10, fravaerListe)
        val filtrert = MeldekortMapper.filtrerMeldekortliste(person, mockMeldekortService)

        assert(filtrert.meldekortListe?.size == 1)
    }
}