package no.nav.meldeplikt.meldekortservice.utils

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import no.nav.meldeplikt.meldekortservice.model.Meldeperiode
import no.nav.meldeplikt.meldekortservice.model.korriger.KopierMeldekortResponse
import no.nav.meldeplikt.meldekortservice.model.meldekort.Person
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.arena.Meldekort
import kotlin.reflect.KClass

private val xmlMapper = XmlMapper()

/*fun mapPersonXml(person: String): Person {
    return xmlMapper.readValue(person, Person::class.java)
}

fun mapMeldekortXml(detaljer: String): Meldekort {
    return xmlMapper.readValue(detaljer, Meldekort::class.java)
}

fun mapMeldekortIdXml(nyMeldekortId: String): KopierMeldekortResponse {
    return xmlMapper.readValue(nyMeldekortId, KopierMeldekortResponse::class.java)
}

fun mapMeldeperiodeXml(meldeperiode: String): Meldeperiode {

}*/

fun <T> mapFraXml(xml: String, responseKlasse: Class<T>): T {
    return xmlMapper.readValue(xml, responseKlasse)
}