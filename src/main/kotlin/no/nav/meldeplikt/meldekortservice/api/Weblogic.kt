package no.nav.meldeplikt.meldekortservice.api

import io.ktor.locations.*
import io.ktor.routing.*
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.model.WeblogicPing
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.WEBLOGIC_PING_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrError
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

@KtorExperimentalLocationsAPI
fun Routing.weblogicApi() {
    pingWeblogic()
}

@Group("Weblogic")
@Location("$WEBLOGIC_PING_PATH")
@KtorExperimentalLocationsAPI
class PingWeblogicInput

// Endepunkt for ping av weblogic
@KtorExperimentalLocationsAPI
fun Routing.pingWeblogic() =
    get<PingWeblogicInput>(
        "Ping av weblogic for å sjekke at Arena er oppe".responds(
            ok<WeblogicPing>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {
        respondOrError {
            SoapConfig.soapService().pingWeblogic()
        }
    }