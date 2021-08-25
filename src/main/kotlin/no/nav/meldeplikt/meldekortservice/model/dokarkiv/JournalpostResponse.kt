package no.nav.meldeplikt.meldekortservice.model.dokarkiv

// Se https://confluence.adeo.no/display/BOA/opprettJournalpost
data class JournalpostResponse(
    val journalpostId: String,
    val melding: String? = null,
    val journalpostFerdigstilt: Boolean? = null,
    val dokumenter: List<String>? = null
)