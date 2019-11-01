package no.nav.meldeplikt.meldekortservice.api

import io.ktor.locations.Location
import io.ktor.routing.Routing
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.WEBLOGIC_PING_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrServiceUnavailable
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

fun Routing.weblogicApi() {
    pingWeblogic()
}

@Group("Weblogic")
@Location("$WEBLOGIC_PING_PATH")
class PingWeblogicInput

// Endepunkt for ping av weblogic
fun Routing.pingWeblogic() =
    get<PingWeblogicInput>(
        "Ping av weblogic for å sjekke at Arena er oppe".securityAndReponds(
            BearerTokenSecurity(),
            ok<WeblogicPing>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {
        respondOrServiceUnavailable {
            SoapConfig.soapService().pingWeblogic()
        }
    }