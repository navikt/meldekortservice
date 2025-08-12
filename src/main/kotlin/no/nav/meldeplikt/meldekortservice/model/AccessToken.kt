package no.nav.meldeplikt.meldekortservice.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AccessToken(
    @param:JsonProperty("access_token")
    val accessToken: String?,
    @param:JsonProperty("token_type")
    val tokenType: String?,
    @param:JsonProperty("expires_in")
    val expiresIn: Int?
)