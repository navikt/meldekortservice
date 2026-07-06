package no.nav.meldeplikt.meldekortservice.utils

import tools.jackson.core.JacksonException
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.ser.std.StdSerializer
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

class LocalDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {

    @Throws(IOException::class, JacksonException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LocalDate {
        return LocalDate.parse(jp.readValueAs(String::class.java))
    }
}

class LocalDateSerializer : StdSerializer<LocalDate>(LocalDate::class.java) {

    @Throws(IOException::class, JacksonException::class)
    override fun serialize(value: LocalDate, gen: JsonGenerator, ctxt: SerializationContext) {
        gen.writeString(value.format(ISO_LOCAL_DATE))
    }
}

class LocalDateTimeDeserializer : StdDeserializer<LocalDateTime>(LocalDateTime::class.java) {

    @Throws(IOException::class, JacksonException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        return LocalDateTime.parse(jp.readValueAs(String::class.java))
    }
}

class LocalDateTimeSerializer : StdSerializer<LocalDateTime>(LocalDateTime::class.java) {

    @Throws(IOException::class, JacksonException::class)
    override fun serialize(value: LocalDateTime, gen: JsonGenerator, ctxt: SerializationContext) {
        gen.writeString(value.format(ISO_LOCAL_DATE_TIME))
    }
}
