package no.nav.meldeplikt.meldekortservice.api

import com.bettercloud.vault.json.Json
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import no.nav.meldeplikt.meldekortservice.config.SWAGGER_URL_V1
import no.nav.meldeplikt.meldekortservice.config.WeblogicConfig
import no.nav.meldeplikt.meldekortservice.config.hentVaultCredentials
import no.nav.meldeplikt.meldekortservice.config.swagger
import no.nav.meldeplikt.meldekortservice.utils.API_PATH
import no.nav.meldeplikt.meldekortservice.utils.BASE_PATH
import no.nav.meldeplikt.meldekortservice.utils.INTERNAL_PATH
import no.nav.meldeplikt.meldekortservice.utils.swagger.SwaggerUi
import no.nav.tjeneste.virksomhet.arbeidogaktivitetsak.v1.Ping
import org.springframework.http.codec.json.Jackson2JsonEncoder
import java.lang.Exception

fun Route.healthApi() {

    route(INTERNAL_PATH) {

        get("/isAlive") {
            call.respondText(text = "Alive", contentType = ContentType.Text.Plain)
        }

        get("/isReady") {
            call.respondText(text = "Ready", contentType = ContentType.Text.Plain)
        }

        get("/ping") {
            val cred = WeblogicConfig.arbeidOgAktivitetSakV1()
            val pingJsonResponse = try {
                val response = cred.ping(Ping())
                println(response)
                """{"ping": "pong", "weblogic": "$response"}"""
            } catch (e: Exception) {
                println(e)
                """{"ping": "pong", "weblogic": "${e.message}"}"""
            }
            call.respondText(text = pingJsonResponse, contentType = ContentType.Application.Json)
        }
    }
}

@KtorExperimentalAPI
fun Routing.swaggerRoutes() {
    val swaggerUI = SwaggerUi()

    get(BASE_PATH) { call.respondRedirect(SWAGGER_URL_V1) }
    get(API_PATH) { call.respondRedirect(SWAGGER_URL_V1) }
    get("$INTERNAL_PATH/apidocs") { call.respondRedirect(SWAGGER_URL_V1) }
    get("$INTERNAL_PATH/apidocs/{fileName}") {
        val fileName = call.parameters["fileName"]
        if (fileName == "swagger.json") {
            call.respond(swagger)
        } else {
            swaggerUI.serve(fileName, call)
        }
    }
}