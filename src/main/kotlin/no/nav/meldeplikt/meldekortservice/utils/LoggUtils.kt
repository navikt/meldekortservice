package no.nav.meldeplikt.meldekortservice.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun getLogger(name: KClass<*>): Logger = LoggerFactory.getLogger(name.java)
fun getLogger(name: String): Logger = LoggerFactory.getLogger(name)
