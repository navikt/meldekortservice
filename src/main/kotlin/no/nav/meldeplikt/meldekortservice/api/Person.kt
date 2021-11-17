package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.SoapConfig
import no.nav.meldeplikt.meldekortservice.config.userIdent
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortMapper
import no.nav.meldeplikt.meldekortservice.mapper.MeldekortkontrollMapper
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.database.feil.UnretriableDatabaseException
import no.nav.meldeplikt.meldekortservice.model.feil.NoContentException
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.kontroll.response.KontrollResponse
import no.nav.meldeplikt.meldekortservice.model.response.EmptyResponse
import no.nav.meldeplikt.meldekortservice.service.ArenaOrdsService
import no.nav.meldeplikt.meldekortservice.service.InnsendtMeldekortService
import no.nav.meldeplikt.meldekortservice.service.KontrollService
import no.nav.meldeplikt.meldekortservice.utils.*
import no.nav.meldeplikt.meldekortservice.utils.swagger.*

/**
REST-controller for meldekort-api som tilbyr operasjoner for å hente:
- Historiske meldekort
- Meldekort
I tillegg til å sende inn/kontrollere meldekort.
 */
@KtorExperimentalLocationsAPI
fun Routing.personApi(
    arenaOrdsService: ArenaOrdsService,
    innsendtMeldekortService: InnsendtMeldekortService,
    kontrollService: KontrollService
) {
    getHistoriskeMeldekort(arenaOrdsService)
    getMeldekort(arenaOrdsService, innsendtMeldekortService)
    kontrollerMeldekort(innsendtMeldekortService, kontrollService)
}

private val xmlMapper = XmlMapper()
val jsonMapper = jacksonObjectMapper()
private val meldekortkontrollMapper = MeldekortkontrollMapper()

private const val personGroup = "Person"

@Group(personGroup)
@Location("$PERSON_PATH/historiskemeldekort")
@KtorExperimentalLocationsAPI
data class HistoriskeMeldekortInput(val antallMeldeperioder: Int)

// Henter historiske meldekort
@KtorExperimentalLocationsAPI
fun Routing.getHistoriskeMeldekort(arenaOrdsService: ArenaOrdsService) =
    get<HistoriskeMeldekortInput>(
        "Hent tidligere/historiske meldekort".securityAndResponse(
            BearerTokenSecurity(),
            ok<Person>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { historiskeMeldekortInput ->
        respondOrError {
            arenaOrdsService.hentHistoriskeMeldekort(
                userIdent,
                historiskeMeldekortInput.antallMeldeperioder
            )
        }
    }

@Group(personGroup)
@Location("$PERSON_PATH/meldekort")
@KtorExperimentalLocationsAPI
class MeldekortInput

// Henter meldekort
@KtorExperimentalLocationsAPI
fun Routing.getMeldekort(arenaOrdsService: ArenaOrdsService, innsendtMeldekortService: InnsendtMeldekortService) =
    get<MeldekortInput>(
        "Hent meldekort".securityAndResponse(
            BearerTokenSecurity(),
            ok<Person>(),
            noContent<EmptyResponse>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) {
        respondOrError {
            val response = arenaOrdsService.hentMeldekort(userIdent)
            if (response.status == HttpStatusCode.OK) {
                MeldekortMapper.filtrerMeldekortliste(
                    mapFraXml(response.content, Person::class.java),
                    innsendtMeldekortService
                )
            } else {
                throw NoContentException()
            }
        }
    }

// Innsending/kontroll av meldekort (Amelding)
@KtorExperimentalLocationsAPI
fun Routing.kontrollerMeldekort(innsendtMeldekortService: InnsendtMeldekortService, kontrollService: KontrollService) =
    post<MeldekortInput, Meldekortdetaljer>(
        "Kontroll/innsending av meldekort til Amelding".securityAndResponse(
            BearerTokenSecurity(),
            ok<MeldekortKontrollertType>(),
            serviceUnavailable<ErrorMessage>(),
            unAuthorized<Error>()
        )
    ) { meldekortInput: MeldekortInput, meldekort: Meldekortdetaljer ->
        try {
            // Send først kortet til kontroll i meldekort-kontroll. Foreløpig er dette kun for testformål og logging.
            val kontrollResponse = KontrollResponse()
            try {
                val kontrollResponse = kontrollService.kontroller(
                    meldekort = meldekortkontrollMapper.mapMeldekortTilMeldekortkontroll(meldekort)
                )
                if (kontrollResponse.arsakskoder.arsakskode.size > 0) {
                    defaultLog.info(
                        "Kontroll feilet i meldekort-kontroll: " + jsonMapper.writeValueAsString(
                            kontrollResponse
                        )
                    )
                    defaultLog.info(
                        "Feilet meldekort i meldekortkontroll er: " + jsonMapper.writeValueAsString(
                            meldekort
                        )
                    )
                }
            } catch (e: Exception) {
                defaultLog.error("Kunne ikke sende meldekort til meldekort-kontroll: ", e)
            }

            // Send kortet til Amelding (uansett om kontrollen gikk bra eller ikke)
            defaultLog.info(
                "Innkommet meldekort fra meldekort-api er: " + jsonMapper.writeValueAsString(
                    (meldekort)
                )
            )
            val ameldingResponse = SoapConfig.soapService().kontrollerMeldekort(meldekort)
            if (1==1 || ameldingResponse.arsakskoder != null) {
                defaultLog.info(
                    "Kontroll feilet i Amelding: " + jsonMapper.writeValueAsString(
                        ameldingResponse
                    )
                )
                defaultLog.info(
                    "Feilet meldekort i Amelding er: " + jsonMapper.writeValueAsString(
                        maskerFnrIAmeldingMeldekort(meldekort)
                    )
                )
            }

            if (ameldingResponse.status == "OK") {
                try {
                    innsendtMeldekortService.settInnInnsendtMeldekort(InnsendtMeldekort(ameldingResponse.meldekortId))
                } catch (e: UnretriableDatabaseException) {
                    // Meldekort er sendt inn ok til baksystem, men det oppstår feil ved skriving til MIP-tabellen i databasen.
                    // Logger warning, og returnerer ok status til brukeren slik at den ikke forsøker å sende inn meldekortet på
                    // nytt (gir dubletter).
                    val errorMessage =
                        ErrorMessage("Meldekort med id ${meldekort.meldekortId} ble sendt inn, men klarte ikke å skrive til MIP-tabellen. ${e.message}")
                    defaultLog.warn(errorMessage.error, e)
                }
            }
            // Send responsen fra Amelding tilbake som respons
            call.respondText(
                jsonMapper.writeValueAsString(ameldingResponse),
                contentType = ContentType.Application.Json
            )
//            call.respondText(jsonMapper.writeValueAsString(kontrollResponse), contentType = ContentType.Application.Json)
        } catch (e: Exception) {
            val errorMessage =
                ErrorMessage("Meldekort med id ${meldekort.meldekortId} ble ikke sendt inn. ${e.message}")
            defaultLog.error(errorMessage.error, e)
            defaultLog.info(
                "Feilet meldekort i Amelding (exception) er: " + jsonMapper.writeValueAsString(
                    maskerFnrIAmeldingMeldekort(meldekort)
                )
            )
            call.respond(status = HttpStatusCode.ServiceUnavailable, message = errorMessage)
        }
    }

fun maskerFnrIAmeldingMeldekort(meldekort: Meldekortdetaljer): Meldekortdetaljer {
    var maskertMeldekortdetaljer = meldekort
    maskertMeldekortdetaljer.fodselsnr =
        if (maskertMeldekortdetaljer.fodselsnr.length == 11) maskertMeldekortdetaljer.fodselsnr.substring(
            0,
            6
        ) + "*****" else "00000000000"
    return maskertMeldekortdetaljer
}
