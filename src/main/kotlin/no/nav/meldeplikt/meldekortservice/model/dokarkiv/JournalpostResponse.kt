package no.nav.meldeplikt.meldekortservice.model.dokarkiv

import com.fasterxml.jackson.annotation.JsonInclude

// Se https://confluence.adeo.no/display/BOA/opprettJournalpost
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JournalpostResponse(
    val journalpostId: Long, // Dokumentasjon sier at dette feltet er String. Men det ser ut at vi får numerisk ID her
    val journalstatus: String? = null,
    val melding: String? = null,
    val journalpostferdigstilt: Boolean? = null,
    val dokumenter: List<DokumentInfo>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DokumentInfo(
    val dokumentInfoId: Long? = null, // Dokumentasjon sier at dette feltet er String. Men det ser ut at vi får numerisk ID her
)
