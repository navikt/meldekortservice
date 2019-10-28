package no.nav.meldeplikt.meldekortservice.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.locations.Location
import io.ktor.response.respondText
import io.ktor.routing.Routing
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.utils.WEBLOGIC_PING_PATH
import no.nav.meldeplikt.meldekortservice.utils.swagger.Group
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

fun Routing.weblogicApi() {
    pingWeblogic()
}

@Group("Weblogic")
@Location("$WEBLOGIC_PING_PATH")
class WeblogicInput

fun Routing.pingWeblogic() =
    get<WeblogicInput>(
        "Ping av weblogic for å sjekke at Arena er oppe".securityAndReponds(
            BearerTokenSecurity(),
            ok<String>(),
            unAuthorized<Error>()
        )
    ) {
        val pingJsonResponse = """{"ping": "${SoapConfig.oppfoelgingPing()}"}"""
        call.respondText(text = pingJsonResponse, contentType = ContentType.Application.Json)
    }