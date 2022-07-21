package no.nav.meldeplikt.meldekortservice.coroutine

import kotlinx.coroutines.*
import no.nav.meldeplikt.meldekortservice.database.*
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
                    val lagretJournalpostId = triple.first
                    val journalpost = triple.second
                    val retries = triple.third

                    try {
                        // Det er mulig at vi får feil et sted her, dvs. f.eks. når vi lagrer informasjon om at en journalpost har blitt opprettet
                        // (noe med DB eller connection timeout før vi får JournalpostRepspose tilbake)
                        // Da prøver på nytt. Men journalposten eksisterer allerede, vi bare vet ikke om dette
                        // Hva skjer hvis vi prøver å opprette journalpost som allerede eksisterer? No stress.
                        // Hvis journalpost med denne eksternReferanseId allerede eksisterer, returnerer createJournalpost 409 Conflict
                        // Men! Sammen men 409 Conflict returneres vanlig JournalpostReponse
                        // Dvs. vi kan lagre journalpostId og dokumentInfoId og slette midlertidig lagret journalpost fra DB

                        // Send
                        val journalpostResponse = dokarkivService.createJournalpost(journalpost)
                        val journalpostId = journalpostResponse.journalpostId
                        val dokumentInfoId = journalpostResponse.dokumenter[0].dokumentInfoId
                        val meldekortId =
                            journalpost.tilleggsopplysninger!!.first { it.nokkel == "meldekortId" }.verdi.toLong()

                        val lagretJournalpostData = openConnection.hentJournalpostData(journalpostId)

                        if (lagretJournalpostData.isEmpty()) {
                            // Lagre journalpostId-meldekortId
                            openConnection.lagreJournalpostData(
                                journalpostId,
                                dokumentInfoId,
                                meldekortId
                            )

                            // Slette midlertidig lagret journalpost
                            openConnection.sletteMidlertidigLagretJournalpost(lagretJournalpostId)
                        } else {
                            val lagretJournalpost = lagretJournalpostData[0]

                            if (lagretJournalpost.second == dokumentInfoId && lagretJournalpost.third == meldekortId) {
                                // Slette midlertidig lagret journalpost
                                openConnection.sletteMidlertidigLagretJournalpost(lagretJournalpostId)
                            } else {
                                defaultLog.error("Journalpost med ID $journalpostId eksisterer allerede, men har uforventet dokumentInfoId og meldekortId")
                            }
                        }
                    } catch (e: Exception) {
                        defaultLog.error(e.message)
                        // Kan ikke opprette journalpost igjen. Oppdater teller
                        openConnection.oppdaterMidlertidigLagretJournalpost(lagretJournalpostId, retries + 1)
                        defaultLog.warn("Kan ikke opprette journalpost igjen. Data ID = $lagretJournalpostId, retries = $retries")
                    }
                }

                // Avslutt transaksjonen
                openConnection.commit()
            }

            delay(interval)
        }
    }
}