package no.nav.meldeplikt.meldekortservice.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import java.io.Reader
import java.io.Writer
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.sql.Clob
import java.sql.Connection
import java.sql.DatabaseMetaData
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

fun Connection.lagreJournalpostData(journalpostId: Long, dokumentInfoId: Long, meldekortId: Long): Int =
    prepareStatement("""INSERT INTO OPPRETTEDE_JOURNALPOSTER (journalpostId, dokumentInfoId, meldekortId, created) VALUES (?, ?, ?, ?)""")
        .use {
            it.setLong(1, journalpostId)
            it.setLong(2, dokumentInfoId)
            it.setLong(3, meldekortId)
            it.setLong(4, System.currentTimeMillis() / 1000)
            it.executeUpdate()
        }

fun Connection.lagreJournalpostMidlertidig(journalpost: Journalpost): Int =
    prepareStatement("""INSERT INTO MIDLERTIDIG_LAGREDE_JOURNALPOSTER (id, journalpost, created, retries) VALUES (?, ?, ?, ?)""")
        .use {
            val journalpostBytes = bytesToChars(ObjectMapper().writeValueAsBytes(journalpost))

            val metaData: DatabaseMetaData = this.metaData
            val productName = metaData.databaseProductName

            val clob: Clob
            if (productName == "PostgreSQL") {
                clob = SerialClob(journalpostBytes) // this doesn't work with Oracle
            } else {
                clob = this.createClob() // this doesn't work with PostgreSQL
                val out: Writer = clob.setCharacterStream(1L)
                out.write(journalpostBytes)
                out.flush()
                out.close()
            }

            it.setString(1, journalpost.eksternReferanseId) // Vi vet at det er UUID der
            it.setClob(2, clob)
            it.setLong(3, System.currentTimeMillis() / 1000)
            it.setLong(4, 0)
            it.executeUpdate()
        }

fun Connection.hentMidlertidigLagredeJournalposter(): List<Triple<String, Journalpost, Int>> {
    val list = mutableListOf<Triple<String, Journalpost, Int>>()

    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    // Oracle and H2 by default
    var query = """SELECT id, journalpost, retries FROM MIDLERTIDIG_LAGREDE_JOURNALPOSTER"""
    if (productName == "PostgreSQL") {
        query = """SELECT id, convert_from(lo_get(journalpost::oid), 'UTF8') as journalpost, retries FROM MIDLERTIDIG_LAGREDE_JOURNALPOSTER"""
    }

    this.prepareStatement(query)
        .use { preparedStatement ->
            preparedStatement.executeQuery()
                .use { resultSet ->
                    while (resultSet.next()) {
                        val journalpost = jacksonObjectMapper().readValue(
                            clobToString(resultSet.getCharacterStream("journalpost")),
                            Journalpost::class.java
                        )
                        list.add(Triple(resultSet.getString("id"), journalpost, resultSet.getInt("retries")))
                    }
                }
        }

    return list
}

fun Connection.sletteMidlertidigLagretJournalpost(id: String) =
    prepareStatement("""DELETE FROM MIDLERTIDIG_LAGREDE_JOURNALPOSTER WHERE id = ?""")
        .use {
            it.setString(1, id)
            it.executeUpdate()
        }

fun Connection.oppdaterMidlertidigLagretJournalpost(id: String, retries: Int) =
    prepareStatement("""UPDATE MIDLERTIDIG_LAGREDE_JOURNALPOSTER SET retries = ? WHERE id = ?""")
        .use {
            it.setInt(1, retries)
            it.setString(2, id)
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

// Generic solution for Oracle Clob and PostgreSQL Text
private fun clobToString(reader: Reader): String {
    val buffer = StringBuffer()
    var ch: Int
    while (reader.read().also { ch = it } != -1) {
        buffer.append(ch.toChar())
    }

    return buffer.toString()
}