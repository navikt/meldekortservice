package no.nav.meldeplikt.meldekortservice.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

//
//import no.nav.security.token.support.client.core.ClientProperties
//import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
//import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
//import no.nav.security.token.support.client.spring.ClientConfigurationProperties
//import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
//import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
//import org.springframework.boot.web.client.RestTemplateBuilder
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Profile
//import org.springframework.context.annotation.PropertySource
//import org.springframework.http.client.ClientHttpRequestInterceptor
//import org.springframework.web.client.RestTemplate
//import java.util.*
//import java.util.function.Supplier
//
//
//@EnableJwtTokenValidation
//@EnableOAuth2Client(cacheEnabled = true)
//@Configuration
//internal class OAuth2Configuration {
//    /**
//     * Create one RestTemplate per OAuth2 client entry to separate between different scopes per API
//     */
//    @Bean
//    fun downstreamResourceRestTemplate(
//        restTemplateBuilder: RestTemplateBuilder,
//        clientConfigurationProperties: ClientConfigurationProperties,
//        oAuth2AccessTokenService: OAuth2AccessTokenService
//    ): RestTemplate {
//        val clientProperties: ClientProperties = Optional.ofNullable(
//            clientConfigurationProperties.getRegistration().get("example-clientcredentials")
//        )
//            .orElseThrow(Supplier { RuntimeException("could not find oauth2 client config for example-onbehalfof") })
//        return restTemplateBuilder
//            .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
//            .build()
//    }
//
//    private fun bearerTokenInterceptor(
//        clientProperties: ClientProperties,
//        oAuth2AccessTokenService: OAuth2AccessTokenService
//    ): ClientHttpRequestInterceptor {
//        return ClientHttpRequestInterceptor { request, body, execution ->
//            val response: OAuth2AccessTokenResponse = oAuth2AccessTokenService.getAccessToken(clientProperties)
//            request.getHeaders().setBearerAuth(response.getAccessToken())
//            execution.execute(request, body)
//        }
//    }
//}

@Configuration
//@Profile("local")
@PropertySource("classpath:application-local.secrets.properties")
internal class LocalProfileConfiguration
