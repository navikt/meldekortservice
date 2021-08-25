package no.nav.meldeplikt.meldekortservice.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AccessToken(
    @JsonProperty("access_token")
    val accessToken: String?,
    @JsonProperty("token_type")
    val tokenType: String?,
    @JsonProperty("expires_in")
    val expiresIn: Int?
)