package no.nav.meldeplikt.meldekortservice.utils.swagger

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.ServiceUnavailable
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.resources.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.*
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.isCurrentlyRunningOnNais
import no.nav.meldeplikt.meldekortservice.utils.swagger
import java.util.*
import kotlin.collections.set
import kotlin.reflect.KClass

/**
 * @author Niels Falk, changed by Torstein Nesby, Yrjan Fraschetti and Almir Lisic
 */

sealed class Security
data class NoSecurity(val secSetting: List<Map<String, List<String>>> = emptyList()) : Security()
data class BearerTokenSecurity(
    val secSetting: List<Map<String, List<String>>> = listOf(mapOf("bearerAuth" to emptyList()))
) : Security()

data class Metadata(
    val responses: Map<HttpStatusCode, KClass<*>>,
    val summary: String = "",
    val security: Security = NoSecurity()
) {

    var headers: KClass<*>? = null
    var parameter: KClass<*>? = null

    inline fun <reified T> header(): Metadata {
        this.headers = T::class
        return this
    }

    inline fun <reified T> parameter(): Metadata {
        this.parameter = T::class
        return this
    }
}

inline fun <reified RESOURCE : Any, reified ENTITY_TYPE : Any> Metadata.apply(method: HttpMethod) {
    val clazz = RESOURCE::class.java
    val resource = clazz.getAnnotation(Resource::class.java)
    val tags = clazz.getAnnotation(Group::class.java)
    applyResponseDefinitions()
    applyOperations(resource, tags, method, RESOURCE::class, ENTITY_TYPE::class)
}

fun Metadata.applyResponseDefinitions() =
    responses.values.forEach { addDefinition(it) }

fun <RESOURCE : Any, BODY_TYPE : Any> Metadata.applyOperations(
    resource: Resource,
    group: Group?,
    method: HttpMethod,
    resourceType: KClass<RESOURCE>,
    entityType: KClass<BODY_TYPE>
) {
    swagger.paths
        .getOrPut(resource.path) { mutableMapOf() }[method.value.lowercase(Locale.getDefault())] =
        Operation(this, resource, group, resourceType, entityType, method)

    if (group != null && swagger.tags.find { tag -> tag.name == group.name } == null) {
        swagger.tags.add(Tag(group.name, group.description))
    }
}

fun String.responds(vararg pairs: Pair<HttpStatusCode, KClass<*>>): Metadata =
    Metadata(responses = mapOf(*pairs), summary = this)

fun String.securityAndResponse(security: Security, vararg pairs: Pair<HttpStatusCode, KClass<*>>): Metadata =
    Metadata(responses = mapOf(*pairs), summary = this, security = security)

inline fun <reified T> ok(): Pair<HttpStatusCode, KClass<*>> = OK to T::class
inline fun <reified T> noContent(): Pair<HttpStatusCode, KClass<*>> = NoContent to T::class
inline fun <reified T> failed(): Pair<HttpStatusCode, KClass<*>> = InternalServerError to T::class
inline fun <reified T> serviceUnavailable(): Pair<HttpStatusCode, KClass<*>> = ServiceUnavailable to T::class
inline fun <reified T> badRequest(): Pair<HttpStatusCode, KClass<*>> = BadRequest to T::class
inline fun <reified T> unAuthorized(): Pair<HttpStatusCode, KClass<*>> = Unauthorized to T::class

inline fun <reified RESOURCE : Any, reified ENTITY : Any> Route.post(
    metadata: Metadata,
    noinline body: suspend RoutingContext.(RESOURCE, ENTITY) -> Unit
): Route {

    defaultLog.debug("Generating swagger spec for POST ${RESOURCE::class.java.getAnnotation(Resource::class.java)}")
    metadata.apply<RESOURCE, ENTITY>(HttpMethod.Post)

    return when (metadata.security) {
        is NoSecurity -> post<RESOURCE> { body(this, it, call.receive()) }
        is BearerTokenSecurity ->
            if (isCurrentlyRunningOnNais())
                authenticate { post<RESOURCE> { body(this, it, call.receive()) } }
            else
                post<RESOURCE> { body(this, it, call.receive()) }
    }
}

inline fun <reified RESOURCE : Any, reified ENTITY : Any> Route.put(
    metadata: Metadata,
    noinline body: suspend RoutingContext.(RESOURCE, ENTITY) -> Unit
): Route {

    defaultLog.debug("Generating swagger spec for PUT ${RESOURCE::class.java.getAnnotation(Resource::class.java)}")
    metadata.apply<RESOURCE, ENTITY>(HttpMethod.Put)

    return when (metadata.security) {
        is NoSecurity -> put<RESOURCE> { body(this, it, call.receive()) }
        is BearerTokenSecurity ->
            if (isCurrentlyRunningOnNais())
                authenticate { put<RESOURCE> { body(this, it, call.receive()) } }
            else
                put<RESOURCE> { body(this, it, call.receive()) }
    }
}

inline fun <reified RESOURCE : Any> Route.get(
    metadata: Metadata,
    noinline body: suspend RoutingContext.(RESOURCE) -> Unit
): Route {

    defaultLog.debug("Generating swagger spec for GET ${RESOURCE::class.java.getAnnotation(Resource::class.java)}")
    metadata.apply<RESOURCE, Unit>(HttpMethod.Get)

    return when (metadata.security) {
        is NoSecurity -> get<RESOURCE> { body(this, it) }
        is BearerTokenSecurity ->
            if (isCurrentlyRunningOnNais())
                authenticate { get<RESOURCE> { body(this, it) } }
            else
                get<RESOURCE> { body(this, it) }
    }
}

inline fun <reified RESOURCE : Any> Route.delete(
    metadata: Metadata,
    noinline body: suspend RoutingContext.(RESOURCE) -> Unit
): Route {

    defaultLog.debug("Generating swagger spec for DELETE ${RESOURCE::class.java.getAnnotation(Resource::class.java)}")
    metadata.apply<RESOURCE, Unit>(HttpMethod.Delete)

    return when (metadata.security) {
        is NoSecurity -> delete<RESOURCE> { body(this, it) }
        is BearerTokenSecurity ->
            if (isCurrentlyRunningOnNais())
                authenticate { delete<RESOURCE> { body(this, it) } }
            else
                delete<RESOURCE> { body(this, it) }
    }
}
