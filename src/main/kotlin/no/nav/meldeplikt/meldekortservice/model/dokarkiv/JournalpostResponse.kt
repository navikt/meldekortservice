package no.nav.meldeplikt.meldekortservice.model.dokarkiv

import com.fasterxml.jackson.annotation.JsonInclude

// Se https://confluence.adeo.no/display/BOA/opprettJournalpost
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JournalpostResponse(
    val journalpostId: String,
    val journalstatus: String? = null,
    val melding: String? = null,
    val journalpostferdigstilt: Boolean? = null,
    val dokumenter: List<DokumentInfo>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DokumentInfo(
    val dokumentInfoId: String? = null,
)
