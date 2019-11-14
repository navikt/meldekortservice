package no.nav.meldeplikt.meldekortservice.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import no.aetat.arena.mk_meldekort_kontrollert.MeldekortKontrollertType
import no.nav.meldeplikt.meldekortservice.config.SWAGGER_URL_V1
import no.nav.meldeplikt.meldekortservice.config.swagger
import no.nav.meldeplikt.meldekortservice.utils.API_PATH
import no.nav.meldeplikt.meldekortservice.utils.BASE_PATH
import no.nav.meldeplikt.meldekortservice.utils.INTERNAL_PATH
import no.nav.meldeplikt.meldekortservice.utils.swagger.SwaggerUi

fun Route.healthApi() {

    route(INTERNAL_PATH) {

        get("/isAlive") {
            call.respondText(text = "Alive", contentType = ContentType.Text.Plain)
        }

        get("/isReady") {
            call.respondText(text = "Ready", contentType = ContentType.Text.Plain)
        }

        get("/ping") {
            //val pingJsonResponse = """{"ping": "pong"}"""
            call.respondText(text = test, contentType = ContentType.Application.Json)
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

fun hentResponse(): MeldekortKontrollertType = jacksonObjectMapper().registerModule(KotlinModule()).readValue(test, MeldekortKontrollertType::class.java)

val test = """{
  "meldekortId": 1377264856,
  "status": "FEIL",
  "arsakskoder": {
    "arsakskode": [
      {
        "kode": "FA00",
        "tekst": "Årsakskodebeskrivelse ikke funnet (internfeil)"
      },
      {
        "kode": "SF00",
        "tekst": "Årsakskodebeskrivelse ikke funnet (internfeil)"
      },
      {
        "kode": "FA21",
        "tekst": "Krysset av både for arbeid og annet fravær enn sykdom i hele perioden"
      },
      {
        "kode": "SF21",
        "tekst": "Krysset av for både sykedager og annet fravær enn sykdom i hele perioden"
      }
    ]
  },
  "meldekortDager": {
    "meldekortDag": [
      {
        "dag": 0,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 1,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 2,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 3,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 4,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 5,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 6,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 7,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 8,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 9,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 10,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 11,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 12,
        "meldegruppe": "ATTF"
      },
      {
        "dag": 13,
        "meldegruppe": "ATTF"
      }
    ]
  }
}"""