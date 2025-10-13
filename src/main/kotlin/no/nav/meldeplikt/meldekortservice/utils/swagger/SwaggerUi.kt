package no.nav.meldeplikt.meldekortservice.utils.swagger

import io.ktor.http.ContentType
import io.ktor.http.content.URIFileContent
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondText

/**
 * @author Niels Falk, changed by Torstein Nesby and Yrjan Fraschetti
 */
class SwaggerUi {

    private val notFound = mutableListOf<String>()
    private val content = mutableMapOf<String, URIFileContent>()

    suspend fun serve(filename: String?, call: ApplicationCall) {
        when (filename) {
            in notFound -> return
            null -> return
            else -> {
                val resource = this::class.java.getResource("/META-INF/resources/webjars/swagger-ui/5.29.3/$filename")
                if (resource == null) {
                    notFound.add(filename)
                    return
                }

                // OBS! Etter SwaggerUI 4.1.2 kan man ikke sende "url"-parameteren gjennom query string uten å sette queryConfigEnabled=true
                // Uten "url"-parameteren åpnes SwaggerUI med dummy Petstore swagger.json
                // Jeg kan ikke finne ut hvordan queryConfigEnabled=true kan settes siden vi bruker allerede ferdige JS-filer fra prekompilerte webjars
                // Derfor må vi endre configen "post factum" i JS, dvs. erstatte dummy Petstore URL med vår swagger.json
                if (filename == "swagger-initializer.js") {
                    val originalBody = resource.readText()
                    val newBody = originalBody.replace(
                        "https://petstore.swagger.io/v2/swagger.json",
                        "swagger.json"
                    )

                    call.respondText(newBody, ContentType.Text.Html)
                    return
                }

                call.respond(content.getOrPut(filename) { URIFileContent(resource) })
            }
        }
    }
}
