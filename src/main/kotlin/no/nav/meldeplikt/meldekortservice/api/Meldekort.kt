package no.nav.meldeplikt.meldekortservice.api

import io.ktor.locations.Location
import io.ktor.routing.Routing
import no.nav.meldeplikt.meldekortservice.swagger.*
import no.nav.meldeplikt.meldekortservice.utils.Error
import no.nav.meldeplikt.meldekortservice.utils.ErrorMessage
import no.nav.meldeplikt.meldekortservice.utils.MELDEKORT_PATH
import no.nav.meldeplikt.meldekortservice.utils.respondOrServiceUnavailable

fun Routing.meldekortApi() {
    getMeldekortdetaljer()
    getKorrigertMeldekort()
}

private const val meldekortGroup = "Meldekort"

@Group(meldekortGroup)
@Location("$MELDEKORT_PATH/{meldekortId}")
data class GetMeldekortdetaljer(val meldekortId: Long)

fun Routing.getMeldekortdetaljer() =
    get<GetMeldekortdetaljer>(
        "Hent meldekortdetaljer".securityAndReponds(BearerTokenSecurity(), ok<String>(),
            serviceUnavailable<ErrorMessage>(), unAuthorized<Error>())) { meldekortid ->
        respondOrServiceUnavailable {
            "Hent meldekortdetaljer er ikke implementert, men id var: ${meldekortid.meldekortId}"
        }
    }

@Group(meldekortGroup)
@Location("$MELDEKORT_PATH/{meldekortId}/korrigering")
data class GetKorrigertMeldekort(val meldekortId: Long)

fun Routing.getKorrigertMeldekort() =
    get<GetKorrigertMeldekort>(
        "Hent korrigert meldekortid".securityAndReponds(BearerTokenSecurity(), ok<String>(),
            serviceUnavailable<ErrorMessage>(), unAuthorized<Error>())) { meldekortid ->
        respondOrServiceUnavailable {
            "Hent korrigert id er ikke implementert, men id var: ${meldekortid.meldekortId}"
        }
    }

/*
fun Route.meldekortApi(httpClient: HttpClient) {

    route("/meldekort") {

        intercept(ApplicationCallPipeline.Setup) {
            try {
                call.parameters["id"]!!.toLongOrNull() ?: throw IllegalArgumentException("Fant ikke id eller id var ikke et nummer")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                return@intercept finish()
            }
        }

        // Hent meldekortdetaljer
        get("/{id}") {
            val id = call.parameters["id"]!!.toLong()
            call.respondText(
                text = "Hent meldekortdetaljer er ikke implementert, men id var: $id",
                contentType = ContentType.Text.Plain
            )
        }

        get("/{id}/korrigering") {
            val id = call.parameters["id"]!!.toLong()
            call.respondText(
                text = "Hent korrigert id er ikke implementert, men id var: $id",
                contentType = ContentType.Text.Plain
            )
        }
    }
}*/
