@file:Suppress("MemberVisibilityCanPrivate", "unused")

package no.nav.meldeplikt.meldekortservice.utils.swagger

import com.fasterxml.jackson.annotation.JsonInclude
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import no.nav.meldeplikt.meldekortservice.utils.swagger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

/**
 * @author Niels Falk, changed by Torstein Nesby, Yrjan Fraschetti and Almir Lisic
 */

typealias ModelName = String
typealias PropertyName = String
typealias Path = String
typealias Definitions = MutableMap<ModelName, ModelData>
typealias Paths = MutableMap<Path, Methods>
typealias MethodName = String
typealias HttpStatus = String
typealias Methods = MutableMap<MethodName, Operation>
typealias Content = MutableMap<String, MutableMap<String, ModelReference?>>

data class Key(
    val description: String,
    val type: String,
    val name: String,
    val `in`: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Swagger(
    val openapi: String = "3.0.0",
    val info: Information,
    val tags: MutableList<Tag> = mutableListOf(),
    val paths: Paths = mutableMapOf(),
    val components: Components = Components(
        SecuritySchemes(
            BearerAuth(
                type = "http",
                scheme = "bearer",
                bearerFormat = "JWT"
            )
        )
    )
)

data class Components(
    val securitySchemes: SecuritySchemes,
    val schemas: Definitions = mutableMapOf()
)

data class SecuritySchemes(
    val bearerAuth: BearerAuth
)

data class BearerAuth(
    val type: String,
    val scheme: String,
    val bearerFormat: String
)

data class Information(
    val description: String,
    val version: String,
    val title: String,
    val contact: Contact
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Contact(
    val name: String? = null,
    val url: String? = null,
    val email: String? = null
)

data class Tag(
    val name: String,
    val description: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class Operation(
    metadata: Metadata,
    resource: Resource,
    group: Group?,
    resourceType: KClass<*>,
    entityType: KClass<*>,
    method: HttpMethod
) {
    val tags = group?.toList() // Operation tags are not objects, but strings, don't mix with toplevel tags
    val summary = metadata.summary
    val parameters = setParameterList(entityType, resourceType, resource, metadata, method)
    val requestBody = setRequestBody(entityType, resourceType, resource, metadata, method)

    val responses: Map<HttpStatus, Response> = metadata.responses.map {
        val (status, kClass) = it
        addDefinition(kClass)
        status.value.toString() to Response(status, kClass)
    }.toMap()

    val security = when (metadata.security) {
        is NoSecurity -> metadata.security.secSetting
        is BearerTokenSecurity -> metadata.security.secSetting
    }
}

private fun setRequestBody(
    entityType: KClass<*>,
    resourceType: KClass<*>,
    resource: Resource,
    metadata: Metadata,
    method: HttpMethod
): Any? {
    if (method.value == "POST" || method.value == "PUT") {
        return mutableListOf<RequestBody>().apply {
            if (entityType != Unit::class) {
                addDefinition(entityType)
                add(entityType.bodyRequest())
            }
            addAll(resourceType.memberProperties.map { it.toRequestBody(resource.path) })
            metadata.parameter?.let { param ->
                addAll(param.memberProperties.map { it.toRequestBody(resource.path, ParameterInputType.Query) })
            }
            metadata.headers?.let { header ->
                addAll(header.memberProperties.map { it.toRequestBody(resource.path, ParameterInputType.Header) })
            }
        }.firstOrNull() ?: emptyList<RequestBody>()
    } else {
        return null
    }
}

private fun setParameterList(
    entityType: KClass<*>,
    resourceType: KClass<*>,
    resource: Resource,
    metadata: Metadata,
    method: HttpMethod
): List<Parameter> {
    if (method.value == "GET" || method.value == "DELETE" || method.value == "HEAD") {
        return mutableListOf<Parameter>().apply {
            if (entityType != Unit::class) {
                addDefinition(entityType)
                add(entityType.bodyParameter())
            }
            addAll(resourceType.memberProperties.map { it.toParameter(resource.path) })
            metadata.parameter?.let { param ->
                addAll(param.memberProperties.map { it.toParameter(resource.path, ParameterInputType.Query) })
            }
            metadata.headers?.let { header ->
                addAll(header.memberProperties.map { it.toParameter(resource.path, ParameterInputType.Header) })
            }
        }
    } else {
        return emptyList()
    }
}

private fun Group.toList(): List<String> {
    return listOf(name)
}

fun <T, R> KProperty1<T, R>.toParameter(
    path: String,
    inputType: ParameterInputType =
        if (path.contains("{$name}"))
            ParameterInputType.Path
        else
            ParameterInputType.Query
): Parameter {
    val property = toModelProperty()

    return Parameter(
        property,
        name,
        inputType.name.lowercase(),
        required = !returnType.isMarkedNullable,
        type = if (inputType == ParameterInputType.Query) null else property.type,
        format = if (inputType == ParameterInputType.Query) null else property.format
    )
}

private fun KClass<*>.bodyParameter() =
    Parameter(
        referenceProperty(),
        name = "body",
        description = modelName(),
        `in` = ParameterInputType.Body.name.lowercase()
    )

fun <T, R> KProperty1<T, R>.toRequestBody(
    path: String,
    inputType: ParameterInputType =
        if (path.contains("{$name}"))
            ParameterInputType.Path
        else
            ParameterInputType.Query
): RequestBody {
    return RequestBody(
        toModelProperty(),
        required = !returnType.isMarkedNullable
    )
}

private fun KClass<*>.bodyRequest() =
    RequestBody(
        referenceProperty(),
        description = modelName()
    )

class Response(httpStatusCode: HttpStatusCode, kClass: KClass<*>) {
    val description = if (kClass == Unit::class) httpStatusCode.description else kClass.responseDescription()
    val content: MutableMap<String, MutableMap<String, ModelReference?>> = mutableMapOf(
        "application/json" to mutableMapOf(
            "schema" to if (kClass == Unit::class) null else ModelReference("#/components/schemas/" + kClass.modelName())
        )
    )
}

fun KClass<*>.responseDescription(): String = modelName()

class ModelReference(val `$ref`: String)

class RequestBody(
    property: Property,
    val description: String = property.description,
    val required: Boolean = true,
    val content: MutableMap<String, MutableMap<String, ModelReference>> = mutableMapOf(
        "application/json" to mutableMapOf(
            "schema" to ModelReference(property.`$ref`)
        )
    )
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class Parameter(
    property: Property,
    val name: String,
    val `in`: String,
    val description: String = property.description,
    val required: Boolean = true,
    val type: String? = property.type,
    val format: String? = property.format,
    val enum: List<String>? = property.enum,
    val items: Property? = property.items,
    val schema: ModelReference? = ModelReference(property.`$ref`)
)

enum class ParameterInputType {
    Query, Path, Body, Header
}

class ModelData(kClass: KClass<*>) {
    val properties: Map<PropertyName, Property> =
        kClass.memberProperties.associate { it.name to it.toModelProperty() }
}

private const val DATE_TIME: String = "date-time"

val propertyTypes = mapOf(
    Int::class to Property("integer", "int32"),
    Long::class to Property("integer", "int64"),
    String::class to Property("string"),
    Double::class to Property("number", "double"),
    Instant::class to Property("string", DATE_TIME),
    Date::class to Property("string", DATE_TIME),
    LocalDateTime::class to Property("string", DATE_TIME),
    LocalDate::class to Property("string", "date")
).mapKeys { it.key.qualifiedName }

fun <T, R> KProperty1<T, R>.toModelProperty(): Property =
    (returnType.classifier as KClass<*>)
        .toModelProperty(returnType)

private fun KClass<*>.toModelProperty(returnType: KType? = null): Property =
    propertyTypes[qualifiedName?.removeSuffix("?")]
        ?: if (returnType != null && (isSubclassOf(Collection::class) || this.isSubclassOf(Set::class))) {
            val kClass: KClass<*> = returnType.arguments.first().type?.classifier as KClass<*>
            Property(items = kClass.toModelProperty(), type = "array")
        } else if (returnType != null && this.isSubclassOf(Map::class)) {
            Property(type = "object")
        } else if (returnType != null && this.isSubclassOf(String::class)) {
            Property(type = "string")
        } else if (java.isEnum) {
            val enumConstants = (this).java.enumConstants
            Property(enum = enumConstants.map { (it as Enum<*>).name }, type = "string")
        } else {
            addDefinition(this)
            referenceProperty()
        }

private fun KClass<*>.referenceProperty(): Property =
    Property(
        `$ref` = "#/components/schemas/" + modelName(),
        description = modelName(),
        type = null
    )

@JsonInclude(JsonInclude.Include.NON_NULL)
open class Property(
    val type: String?,
    val format: String = "",
    val enum: List<String>? = null,
    val items: Property? = null,
    val description: String = "",
    val `$ref`: String = ""
)

fun addDefinition(kClass: KClass<*>) {
    if ((kClass != Unit::class) && !swagger.components.schemas.containsKey(kClass.modelName())) {
        defaultLog.debug("Generating swagger spec for model ${kClass.modelName()}")
        swagger.components.schemas[kClass.modelName()] = ModelData(kClass)
    }
}

private fun KClass<*>.modelName(): ModelName = simpleName ?: toString()

annotation class Group(val name: String, val description: String = "")
