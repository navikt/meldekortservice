package no.nav.meldeplikt.meldekortservice.coroutine

import kotlinx.coroutines.*
import no.nav.meldeplikt.meldekortservice.database.hentMidlertidigLagredeJournalposter
import no.nav.meldeplikt.meldekortservice.database.lagreJournalpostData
import no.nav.meldeplikt.meldekortservice.database.oppdaterMidlertidigLagretJournalpost
import no.nav.meldeplikt.meldekortservice.database.sletteMidlertidigLagretJournalpost
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
            // Connection må ha autoCommit = false!
            dbService.getConnection().use { openConnection ->
                // Lese data fra DB
                // Triple: data id, journalpost, retries
                val journalpostData: List<Triple<String, Journalpost, Int>> = openConnection.hentMidlertidigLagredeJournalposter()

                journalpostData.forEach { triple ->
                    try {
                        val journalpost = triple.second

                        // Det er mulig at vi får feil et sted her, dvs. f.eks. når vi lagrer informasjon om at en journalpost har blitt opprettet
                        // (noe med DB eller connection timeout før vi får JournalpostRepspose tilbake)
                        // Da prøver på nytt. Men journalposten eksisterer allerede, vi bare vet ikke om dette
                        // Hva skjer hvis vi prøver å opprette journalpost som allerede eksisterer? No stress.
                        // Hvis journalpost med denne eksternReferanseId allerede eksisterer, returnerer createJournalpost 409 Conflict
                        // Men! Sammen men 409 Conflict returneres vanlig JournalpostReponse
                        // Dvs. vi kan lagre journalpostId og dokumentInfoId og slette midlertidig lagret journalpost fra DB

                        // Send
                        val journalpostResponse = dokarkivService.createJournalpost(journalpost)

                        // Lagre journalpostId-meldekortId
                        openConnection.lagreJournalpostData(
                            journalpostResponse.journalpostId,
                            journalpostResponse.dokumenter[0].dokumentInfoId,
                            journalpost.tilleggsopplysninger!!.first { it.nokkel == "meldekortId" }.verdi.toLong()
                        )

                        // Slette midlertidig lagret journalpost
                        openConnection.sletteMidlertidigLagretJournalpost(triple.first)
                    } catch (e: Exception) {
                        defaultLog.error(e.message)
                        // Kan ikke opprette journalpost igjen. Oppdater teller
                        openConnection.oppdaterMidlertidigLagretJournalpost(triple.first, triple.third + 1)
                        defaultLog.warn("Kan ikke opprette journalpost igjen. Data ID = ${triple.first}, retries = ${triple.third}")
                    }
                }

                // Avslutt transaksjonen
                openConnection.commit()
            }

            delay(interval)
        }
    }
}