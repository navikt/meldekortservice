package no.nav.meldeplikt.meldekortservice.model.dokarkiv

import com.fasterxml.jackson.annotation.JsonInclude

// Se https://confluence.adeo.no/display/BOA/opprettJournalpost
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JournalpostResponse(
    val journalpostId: Long, // Dokumentasjon sier at dette feltet er String. Men det ser ut at vi får numerisk ID her
    val journalstatus: String,
    val melding: String? = null,
    val journalpostferdigstilt: Boolean,
    val dokumenter: List<DokumentInfo>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DokumentInfo(
    val dokumentInfoId: Long, // Dokumentasjon sier at dette feltet er String. Men det ser ut at vi får numerisk ID her
)
