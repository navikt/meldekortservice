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
            val journalpostData: List<Triple<String, Journalpost, Int>> = dbService.hentMidlertidigLagredeJournalposter()

            journalpostData.forEach { triple ->
                try {
                    val journalpost = triple.second

                    // TODO: Check that journalpost with eksternalreferenceId doesn't exists
                    // It is possible that we have had an error somewhere here, i.e. when saving info that a journalpost has been created
                    // So we need to check that such journalpost doesn't exists before trying to send data again
                    // We must use saf REST API

                    // Send
                    val journalpostResponse = dokarkivService.createJournalpost(triple.second)

                    // Lagre journalpostId-meldekortId
                    dbService.lagreJournalpostData(
                        journalpostResponse.journalpostId,
                        journalpostResponse.dokumenter[0].dokumentInfoId,
                        journalpost.tilleggsopplysninger!!.first { it.nokkel == "meldekortId" }.verdi.toLong()
                    )

                    // Slette journalpost data
                    dbService.sletteMidlertidigLagretJournalpost(triple.first)
                } catch (e: Exception) {
                    // Kan ikke opprette journalpost igjen. Oppdater teller
                    dbService.oppdaterMidlertidigLagretJournalpost(triple.first, triple.third + 1)
                    defaultLog.warn("Kan ikke opprette journalpost igjen. Data ID = ${triple.first}, retries = ${triple.third}")
                }
            }

            delay(interval)
        }
    }
}