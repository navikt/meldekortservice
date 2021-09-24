package no.nav.meldeplikt.meldekortservice.coroutine

import kotlinx.coroutines.*
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.service.DBService
import no.nav.meldeplikt.meldekortservice.service.DokarkivService
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class SendJournalposterPaaNytt(
    private val dbService: DBService,
    private val dokarkivService: DokarkivService,
    private val interval: Long,
    private val initialDelay: Long?
) : CoroutineScope {
    private val job = Job()

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()


    fun stop() {
        job.cancel()
        singleThreadExecutor.shutdown()
    }

    fun start() = launch {
        initialDelay?.let {
            delay(it)
        }
        while (isActive) {
            // Lese data fra DB
            // Triple: data id, journalpost, retries
            val journalpostData: List<Triple<String, Journalpost, Int>> = dbService.hentJournalpostData()

            journalpostData.forEach { triple ->
                try {
                    val journalpost = triple.second

                    // Send
                    val journalpostResponse = dokarkivService.createJournalpost(triple.second)

                    // Lagre journalpostId-meldekortId
                    dbService.lagreJournalpostMeldekortPar(
                        journalpostResponse.journalpostId,
                        journalpost.tilleggsopplysninger!!.first { it.nokkel == "meldekortId" }.verdi.toLong()
                    )

                    // Slette journalpost data
                    dbService.sletteJournalpostData(triple.first)
                } catch (e: Exception) {
                    // Kan ikke opprette journalpost igjen. Oppdater teller
                    dbService.oppdaterJournalpost(triple.first, triple.third + 1)
                    defaultLog.warn("Kan ikke opprette journalpost igjen. Data ID = ${triple.first}, retries = ${triple.third}")
                }
            }

            delay(interval)
        }
        println("coroutine done")
    }
}