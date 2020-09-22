package no.nav.meldeplikt.meldekortservice.utils

import org.opensaml.storage.annotation.Value
import java.util.*


class TokenUtils {

//    private val accessTokenUri: String? =  "https://login.microsoftonline.com/${TENANT_ID}/oauth2/v2.0/token"
    private val accessTokenUri: String? =  "https://login.microsoftonline.com/oauth2/v2.0/token"

//    @Value("#{ @environment['oauth2.client.clientId'] }")
    private val clientId: String? = null

    //    @Value("#{ @environment['oauth2.client.grantType'] }")
    private val clientGrantType = "client_credentials"

//    @Value("#{ @environment['oauth2.client.clientSecret'] }")
    private val clientSecret: String? = null

    //    @Value("#{ @environment['oauth2.client.scope'] }")
    private val clientScope = "custom_mod"

    private val tokenExpire: Date? = null
    private val lastToken: String? = null

    fun getAccessTokenAsString(): String? {
        val now = Date()
        var uri: String? = accessTokenUri;
//        if (tokenExpire == null || now.after(tokenExpire)) {
//            val oAuth2RestTemplate = OAuth2RestTemplate(oAuthDetails(uri))
//            val accessToken: OAuth2AccessToken = oAuth2RestTemplate.getAccessToken()
//            lastToken= accessToken.getValue()
//            tokenExpire=accessToken.getExpiration()
////            log.info("Hentet nytt token fra Oauth2");
//        } else {
////            log.info("Gjenbrukte token. Det går ut " + tokenExpire.get(miljoe));
//        }
        return lastToken;
    }

//    //    @ConfigurationProperties("oauth2.client")
//    protected fun oAuthDetails(uri: String?): ClientCredentialsResourceDetails? {
//        val resourceDetails = ClientCredentialsResourceDetails()
//        resourceDetails.setClientId(getClientId())
//        resourceDetails.setClientSecret(getClientSecret())
//        resourceDetails.setGrantType(getClientGrantType())
//        resourceDetails.setScope(Arrays.asList(getClientScope()))
//        resourceDetails.setAccessTokenUri(uri)
//        return resourceDetails
//    }
}