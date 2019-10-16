package no.nav.meldeplikt.meldekortservice.model

import com.google.gson.annotations.SerializedName

data class OrdsToken(
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("token_type")
    val tokenType: String?,
    @SerializedName("expires_in")
    val expiresIn: Int?
)