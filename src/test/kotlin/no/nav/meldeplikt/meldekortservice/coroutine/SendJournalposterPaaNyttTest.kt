package no.nav.meldeplikt.meldekortservice.coroutine

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.meldeplikt.meldekortservice.database.H2Database
import no.nav.meldeplikt.meldekortservice.database.hentAlleMidlertidigLagredeJournalposter
import no.nav.meldeplikt.meldekortservice.database.hentJournalpostData
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.DokumentInfo
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.JournalpostResponse
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SendJournalposterPaaNyttTest {
    private val journalpostJson = this::class.java.getResource("/journalpost.json")!!.readText()
    private val journalpost = jacksonObjectMapper().readValue(
        journalpostJson,
        Journalpost::class.java
    )

    @Test
    fun `skal sende journalpost, lagre journalpost data og slette midlertidig journalpost naar OK`() {
        val database = H2Database("journalposttest1")
        val dbService = DBService(database)

        val journalpostId = 123456780L
        val dokumentInfoId = 123456781L
        val journalpostResponse = JournalpostResponse(
            journalpostId = journalpostId,
            journalstatus = "M",
            melding = "MELDING FRA DOKARKIV",
            journalpostferdigstilt = true,
            dokumenter = listOf(
                DokumentInfo(dokumentInfoId)
            )
        )

        val dokarkivService = mockk<DokarkivService>()
        coEvery { dokarkivService.createJournalpost(any()) } returns journalpostResponse

        // Sjekk at det ikke finnes midlertidig lagrede journalposter
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(0, journalpostData.size)
        }

        // Lagre journalpost
        dbService.lagreJournalpostMidlertidig(journalpost)

        // Sjekk at det finnes 1 midlertidig lagret journalpost med 0 i retries
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
            assertEquals(0, journalpostData[0].second) // Retries
        }

        // Sjekk at det ikke finnes journalpost data
        runBlocking {
            val result = database.dbQuery { hentJournalpostData() }
            assertEquals(0, result.size)
        }

        // Prøv å sende på nytt
        val sendJournalposterPaaNytt = SendJournalposterPaaNytt(dbService, dokarkivService, 10_000L, 0)
        sendJournalposterPaaNytt.start()
        Thread.sleep(1_000)
        sendJournalposterPaaNytt.stop()

        // Sjekk at det ikke finnes midlertidig lagrede journalposter
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(0, journalpostData.size)
        }

        // Sjekk at det finnes journalpost data
        runBlocking {
            val result = database.dbQuery { hentJournalpostData() }
            assertEquals(1, result.size)
            assertEquals(journalpostId, result[0].first)
            assertEquals(dokumentInfoId, result[0].second)
            assertEquals(1011121315, result[0].third) // MeldekortId kommer fra journalpost.json
        }

        database.closeConnection()
    }

    @Test
    fun `skal sende journalpost, lagre journalpost data og slette midlertidig journalpost naar allerede eksisterer`() {
        val database = H2Database("journalposttest2")
        val dbService = DBService(database)

        val journalpostId = 123456782L
        val dokumentInfoId = 123456783L
        val meldekortId = 1011121315L // MeldekortId kommer fra journalpost.json
        val journalpostResponse = JournalpostResponse(
            journalpostId = journalpostId,
            journalstatus = "M",
            melding = "MELDING FRA DOKARKIV",
            journalpostferdigstilt = true,
            dokumenter = listOf(
                DokumentInfo(dokumentInfoId)
            )
        )

        val dokarkivService = mockk<DokarkivService>()
        coEvery { dokarkivService.createJournalpost(any()) } returns journalpostResponse

        // Lagre journalpost data
        dbService.lagreJournalpostData(journalpostId, dokumentInfoId, meldekortId)

        // Lagre journalpost midlertidig
        dbService.lagreJournalpostMidlertidig(journalpost)

        // Sjekk at det finnes 1 midlertidig lagret journalpost med 0 i retries
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
            assertEquals(0, journalpostData[0].second) // Retries
        }

        // Prøv å sende på nytt
        val sendJournalposterPaaNytt = SendJournalposterPaaNytt(dbService, dokarkivService, 10_000L, 0)
        sendJournalposterPaaNytt.start()
        Thread.sleep(1_000)
        sendJournalposterPaaNytt.stop()

        // Sjekk at det ikke finnes midlertidig lagrede journalposter
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(0, journalpostData.size)
        }

        // Sjekk at det finnes journalpost data
        runBlocking {
            val result = database.dbQuery { hentJournalpostData() }
            assertEquals(1, result.size)
            assertEquals(journalpostId, result[0].first)
            assertEquals(dokumentInfoId, result[0].second)
            assertEquals(meldekortId, result[0].third)
        }

        database.closeConnection()
    }

    @Test
    fun `skal ikke slette midlertidig journalpost naar det er feil i journalpost data`() {
        val database = H2Database("journalposttest3")
        val dbService = DBService(database)

        val journalpostId = 123456782L
        val dokumentInfoId = 123456783L
        val annendokumentInfoId = 123456784L
        val meldekortId = 1011121315L // MeldekortId kommer fra journalpost.json
        val journalpostResponse = JournalpostResponse(
            journalpostId = journalpostId,
            journalstatus = "M",
            melding = "MELDING FRA DOKARKIV",
            journalpostferdigstilt = true,
            dokumenter = listOf(
                DokumentInfo(dokumentInfoId)
            )
        )

        val dokarkivService = mockk<DokarkivService>()
        coEvery { dokarkivService.createJournalpost(any()) } returns journalpostResponse

        // Lagre journalpost data med en annen dokumentInfoId
        dbService.lagreJournalpostData(journalpostId, annendokumentInfoId, meldekortId)

        // Lagre journalpost midlertidig
        dbService.lagreJournalpostMidlertidig(journalpost)

        // Sjekk at det finnes 1 midlertidig lagret journalpost med 0 i retries
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
            assertEquals(0, journalpostData[0].second) // Retries
        }

        // Prøv å sende på nytt
        val sendJournalposterPaaNytt = SendJournalposterPaaNytt(dbService, dokarkivService, 10_000L, 0)
        sendJournalposterPaaNytt.start()
        Thread.sleep(1_000)
        sendJournalposterPaaNytt.stop()

        // Sjekk at det fortsatt finnes midlertidig lagrede journalposter
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
        }

        // Sjekk at det finnes journalpost data med en annen dokumentInfoId
        runBlocking {
            val result = database.dbQuery { hentJournalpostData() }
            assertEquals(1, result.size)
            assertEquals(journalpostId, result[0].first)
            assertEquals(annendokumentInfoId, result[0].second)
            assertEquals(meldekortId, result[0].third)
        }

        database.closeConnection()
    }

    @Test
    fun `skal sende journalpost og ikke slette midlertidig journalpost naar ikke OK`() {
        val database = H2Database("journalposttest4")
        val dbService = DBService(database)

        val dokarkivService = mockk<DokarkivService>()
        coEvery { dokarkivService.createJournalpost(any()) } throws Exception()

        // Sjekk at det ikke finnes midlertidig lagrede journalposter
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(0, journalpostData.size)
        }

        // Lagre journalpost
        dbService.lagreJournalpostMidlertidig(journalpost)

        // Sjekk at det finnes 1 midlertidig lagret journalpost med 0 i retries
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
            assertEquals(0, journalpostData[0].second) // Retries
        }

        // Prøv å sende på nytt (får feil)
        val sendJournalposterPaaNytt = SendJournalposterPaaNytt(dbService, dokarkivService, 10_000L, 0)
        sendJournalposterPaaNytt.start()
        Thread.sleep(1_000)
        sendJournalposterPaaNytt.stop()

        // Sjekk at det finnes 1 midlertidig lagret journalpost med 1 i retries
        runBlocking {
            val journalpostData = database.dbQuery { hentAlleMidlertidigLagredeJournalposter() }
            assertEquals(1, journalpostData.size)
            assertEquals(1, journalpostData[0].second) // Retries
        }

        database.closeConnection()
    }
}
