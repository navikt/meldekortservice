package no.nav.meldeplikt.meldekortservice.model

data class OrdsToken(
    val accessToken: String?,
    val tokenType: String?,
    val expiresIn: Int?
)