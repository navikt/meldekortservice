package no.nav.meldeplikt.meldekortservice.utils.swagger

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.response.*

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
                val resource = this::class.java.getResource("/META-INF/resources/webjars/swagger-ui/3.52.5/$filename")
                if (resource == null) {
                    notFound.add(filename)
                    return
                }
                call.respond(content.getOrPut(filename) { URIFileContent(resource) })
            }
        }
    }
}
