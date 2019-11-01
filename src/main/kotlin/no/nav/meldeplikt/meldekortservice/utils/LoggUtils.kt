package no.nav.meldeplikt.meldekortservice.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun getLogger(name: KClass<*>): Logger = LoggerFactory.getLogger(name.java)

val defaultLog: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)