package no.nav.meldeplikt.meldekortservice.database

import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import java.sql.Connection
import java.sql.ResultSet

fun Connection.hentAlleInnsendteMeldekort(): List<InnsendtMeldekort> =
    prepareStatement("""SELECT * FROM INNSENDT_MELDEKORT""")
        .use {
            it.executeQuery().list {
                tilInnsendtMeldekort()
            }
        }

fun Connection.hentInnsendtMeldekort(meldekortId: Long): InnsendtMeldekort =
    prepareStatement("""SELECT * FROM INNSENDT_MELDEKORT WHERE meldekortId = ?""")
        .use {
            it.setLong(1, meldekortId)
            it.executeQuery().singleResult {
                tilInnsendtMeldekort()
            }
        }

fun Connection.opprettInnsendtMeldekort(innsendtMeldekort: InnsendtMeldekort): Int =
    prepareStatement("""INSERT INTO INNSENDT_MELDEKORT (meldekortId) VALUES (?)""")
        .use {
        it.setLong(1, innsendtMeldekort.meldekortId)
        it.executeUpdate()
    }

private fun ResultSet.tilInnsendtMeldekort(): InnsendtMeldekort {
    return InnsendtMeldekort(
        meldekortId = getLong("meldekortId")
    )
}