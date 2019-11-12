package no.nav.meldeplikt.meldekortservice.model.database.feil

class RetriableDatabaseException(message: String, cause: Throwable?) : Exception(message, cause) {}