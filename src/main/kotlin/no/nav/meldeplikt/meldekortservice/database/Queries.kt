package no.nav.meldeplikt.meldekortservice.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import java.io.Reader
import java.io.Writer
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.sql.*
import java.time.Instant
import java.util.*
import javax.sql.rowset.serial.SerialClob


fun Connection.hentInnsendtMeldekort(meldekortId: Long): InnsendtMeldekort =
    prepareStatement("SELECT * FROM innsendt_meldekort WHERE meldekortId = ?")
        .use {
            it.setLong(1, meldekortId)
            it.executeQuery().singleResult {
                tilInnsendtMeldekort()
            }
        }

fun Connection.opprettInnsendtMeldekort(innsendtMeldekort: InnsendtMeldekort): Int =
    prepareStatement("INSERT INTO innsendt_meldekort (meldekortId) VALUES (?)")
        .use {
            it.setLong(1, innsendtMeldekort.meldekortId)
            it.executeUpdate()
        }

fun Connection.hentJournalpostData(journalpostId: Long): List<Triple<Long, Long, Long>> =
    prepareStatement("SELECT journalpostId, dokumentInfoId, meldekortId FROM opprettede_journalposter WHERE journalpostId = ?")
        .use {
            it.setLong(1, journalpostId)
            it.executeQuery().list {
                Triple(
                    this.getLong("journalpostId"),
                    this.getLong("dokumentInfoId"),
                    this.getLong("meldekortId")
                )
            }
        }

fun Connection.lagreJournalpostData(journalpostId: Long, dokumentInfoId: Long, meldekortId: Long): Int =
    prepareStatement("INSERT INTO opprettede_journalposter (journalpostId, dokumentInfoId, meldekortId) VALUES (?, ?, ?)")
        .use {
            it.setLong(1, journalpostId)
            it.setLong(2, dokumentInfoId)
            it.setLong(3, meldekortId)
            it.executeUpdate()
        }

fun Connection.lagreJournalpostMidlertidig(journalpost: Journalpost): Int =
    prepareStatement("INSERT INTO midlertidig_lagrede_journalposter (id, journalpost, retries) VALUES (?, ?, ?)")
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
            it.setLong(3, 0)
            it.executeUpdate()
        }

fun Connection.hentMidlertidigLagredeJournalposter(): List<Triple<String, Journalpost, Int>> {
    val list = mutableListOf<Triple<String, Journalpost, Int>>()

    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    // Select records that are not used by another process (not locked) and lock selected (for update)
    // Oracle default
    var query = "SELECT id, journalpost, retries FROM midlertidig_lagrede_journalposter FOR UPDATE SKIP LOCKED"
    if (productName == "PostgreSQL") {
        query = """SELECT id, convert_from(lo_get(journalpost::oid), 'UTF8') as journalpost, retries 
            |FROM midlertidig_lagrede_journalposter 
            |FOR UPDATE SKIP LOCKED"""
            .trimMargin()
    }
    // H2 doesn't support SKIP LOCKED
    if (productName == "H2") {
        query = "SELECT id, journalpost, retries FROM midlertidig_lagrede_journalposter FOR UPDATE"
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
    prepareStatement("DELETE FROM midlertidig_lagrede_journalposter WHERE id = ?")
        .use {
            it.setString(1, id)
            it.executeUpdate()
        }

fun Connection.oppdaterMidlertidigLagretJournalpost(id: String, retries: Int) =
    prepareStatement("UPDATE midlertidig_lagrede_journalposter SET retries = ? WHERE id = ?")
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

fun Connection.lagreRequest(kallLogg: KallLogg) {
    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    prepareStatement(
        "INSERT INTO kall_logg " +
                "(korrelasjon_id, type, tidspunkt, kall_retning, method, operation, status, kalltid, request, response, logginfo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    )
        .use {
            val requestClob: Clob
            val responseClob: Clob?
            val logginfoClob: Clob

            if (productName == "PostgreSQL" || productName == "H2") {
                requestClob = SerialClob(kallLogg.request.toCharArray())

                responseClob = if (kallLogg.response != null) {
                    SerialClob(kallLogg.response.toCharArray())
                } else {
                    null
                }

                logginfoClob = SerialClob(kallLogg.logginfo.toCharArray())
            } else {
                requestClob = this.createClob()
                val requestWriter: Writer = requestClob.setCharacterStream(1L)
                requestWriter.write(kallLogg.request)
                requestWriter.flush()
                requestWriter.close()

                if (kallLogg.response != null) {
                    responseClob = this.createClob()
                    val responseWriter: Writer = responseClob.setCharacterStream(1L)
                    responseWriter.write(kallLogg.response)
                    responseWriter.flush()
                    responseWriter.close()
                } else {
                    responseClob = null
                }


                logginfoClob = this.createClob()
                val logginfoWriter: Writer = logginfoClob.setCharacterStream(1L)
                logginfoWriter.write(kallLogg.logginfo)
                logginfoWriter.flush()
                logginfoWriter.close()
            }

            it.setString(1, kallLogg.korrelasjonId)
            it.setString(2, kallLogg.type)
            it.setTimestamp(3, Timestamp.valueOf(kallLogg.tidspunkt))
            it.setString(4, kallLogg.kallRetning)
            it.setString(5, kallLogg.method)
            it.setString(6, kallLogg.operation)
            it.setInt(7, kallLogg.status)
            it.setLong(8, kallLogg.kallTid)
            it.setClob(9, requestClob)
            it.setClob(10, responseClob)
            it.setClob(11, logginfoClob)

            it.executeUpdate()
        }
}

fun Connection.lagreResponse(korrelasjonId: String, status: Int, response: String) {
    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    prepareStatement(
        "UPDATE kall_logg SET response = ?, status = ?, kalltid = (? - kalltid) " +
                "WHERE korrelasjon_id = ? AND response IS NULL"
    )
        .use {
            val responseClob: Clob

            if (productName == "PostgreSQL" || productName == "H2") {
                responseClob = SerialClob(response.toCharArray())
            } else {
                responseClob = this.createClob()
                val responseWriter: Writer = responseClob.setCharacterStream(1L)
                responseWriter.write(response)
                responseWriter.flush()
                responseWriter.close()
            }

            it.setClob(1, responseClob)
            it.setInt(2, status)
            it.setLong(3, Instant.now().toEpochMilli())
            it.setString(4, korrelasjonId)

            it.executeUpdate()
        }
}

fun Connection.hentKallLoggFelterListeByKorrelasjonId(korrelasjonId: String): List<KallLogg> {
    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    // Oracle default
    var query = "SELECT " +
            "kall_logg_id, korrelasjon_id, tidspunkt, type, kall_retning, method, operation, status, kalltid, " +
            "request, " +
            "response, " +
            "logginfo " +
            "FROM kall_logg " +
            "WHERE korrelasjon_id = ?"
    if (productName == "PostgreSQL") {
        query = "SELECT " +
                "kall_logg_id, korrelasjon_id, tidspunkt, type, kall_retning, method, operation, status, kalltid, " +
                "convert_from(lo_get(request::oid), 'UTF8') as request," +
                "convert_from(lo_get(response::oid), 'UTF8') as response," +
                "convert_from(lo_get(logginfo::oid), 'UTF8') as logginfo " +
                "FROM kall_logg " +
                "WHERE korrelasjon_id = ?"
    }

    val list = mutableListOf<KallLogg>()
    this.prepareStatement(query)
        .use { preparedStatement ->
            preparedStatement.setString(1, korrelasjonId)
            preparedStatement.executeQuery()
                .use { resultSet ->
                    while (resultSet.next()) {
                        list.add(
                            KallLogg(
                                korrelasjonId = resultSet.getString("korrelasjon_id"),
                                tidspunkt = resultSet.getTimestamp("tidspunkt").toLocalDateTime(),
                                type = resultSet.getString("type"),
                                kallRetning = resultSet.getString("kall_retning"),
                                method = resultSet.getString("method"),
                                operation = resultSet.getString("operation"),
                                status = resultSet.getInt("status"),
                                kallTid = resultSet.getLong("kalltid"),
                                request = clobToString(resultSet.getCharacterStream("request")),
                                response = clobToString(resultSet.getCharacterStream("response")),
                                logginfo = clobToString(resultSet.getCharacterStream("logginfo"))
                            )
                        )
                    }
                }
        }

    return list
}

// It would be better to convert an object to a string and then string to array of chars
// But because of some interceptor that converts FNR into * in test-environment, we have to convert objects to bytes first
private fun bytesToChars(bytes: ByteArray?): CharArray {
    val charBuffer: CharBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes))
    return Arrays.copyOf(charBuffer.array(), charBuffer.limit())
}

// Generic solution for Oracle Clob and PostgreSQL Text
fun clobToString(reader: Reader?): String {
    if (reader == null) {
        return ""
    }

    val buffer = StringBuffer()
    var ch: Int
    while (reader.read().also { ch = it } != -1) {
        buffer.append(ch.toChar())
    }

    return buffer.toString()
}
