package no.nav.meldeplikt.meldekortservice.database

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.ResultSet
import java.util.*
import javax.sql.rowset.serial.SerialClob


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

fun Connection.lagreJournalpostMeldekortPar(journalpostId: Long, meldekortId: Long): Int =
    prepareStatement("""INSERT INTO JOURNALPOST_MELDEKORT (journalpostId, meldekortId, created) VALUES (?, ?, ?)""")
        .use {
            it.setLong(1, journalpostId)
            it.setLong(2, meldekortId)
            it.setLong(3, System.currentTimeMillis() / 1000)
            it.executeUpdate()
        }

fun Connection.lagreJournalpost(journalpost: Journalpost): Int =
    prepareStatement("""INSERT INTO JOURNALPOST (id, journalpost, created, retries) VALUES (?, ?, ?, ?)""")
        .use {
            it.setString(1, UUID.randomUUID().toString())
            it.setClob(2, SerialClob(bytesToChars(ObjectMapper().writeValueAsBytes(journalpost))))
            it.setLong(3, System.currentTimeMillis() / 1000)
            it.setLong(4, 0)
            it.executeUpdate()
        }

fun ResultSet.tilInnsendtMeldekort(): InnsendtMeldekort {
    return InnsendtMeldekort(
        meldekortId = getLong("meldekortId")
    )
}

// It would be better to convert an object to a string and then string to array of chars
// But because of some interceptor that converts FNR into * in test-environment, we have to convert objects to bytes first
private fun bytesToChars(bytes: ByteArray?): CharArray {
    val charBuffer: CharBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes))
    return Arrays.copyOf(charBuffer.array(), charBuffer.limit())
}