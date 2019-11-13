package no.nav.meldeplikt.meldekortservice.database

import java.sql.Connection

fun Connection.slettAlleInnsendteMeldekort() =
    prepareStatement("""DELETE FROM INNSENDT_MELDEKORT""")
        .use { it.execute() }

fun Connection.slettInnsendtMeldekort(meldekortId: Long) =
    prepareStatement("""DELETE FROM INNSENDT_MELDEKORT WHERE meldekortId = ?""")
        .use {
            it.setLong(1, meldekortId)
            it.execute()
        }