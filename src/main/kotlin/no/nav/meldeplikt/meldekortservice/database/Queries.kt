package no.nav.meldeplikt.meldekortservice.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import no.nav.meldeplikt.meldekortservice.model.dokarkiv.Journalpost
import no.nav.meldeplikt.meldekortservice.utils.defaultLog
import java.io.Reader
import java.io.Writer
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.sql.Clob
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

fun Connection.hentTekst(kode: String, sprak: String, fraDato: String): String? = prepareStatement(
    "SELECT verdi " +
            "FROM tekst " +
            "WHERE kode = ? " +
            "AND sprak = ? " +
            "AND fra_dato <= TO_DATE(?, 'YYYY-MM-DD') " +
            "ORDER BY fra_dato DESC"
)
    .use { preparedStatement ->
        preparedStatement.setString(1, kode)
        preparedStatement.setString(2, sprak)
        preparedStatement.setString(3, checkDate(fraDato))

        preparedStatement.executeQuery()
            .use { resultSet ->
                if (resultSet.next()) {
                    clobToString(resultSet.getCharacterStream("verdi"))
                } else {
                    null
                }
            }
    }

fun Connection.hentAlleTekster(sprak: String, fraDato: String): Map<String, String> {
    val out = mutableMapOf<String, String>()

    this.prepareStatement(
        "SELECT t1.kode, t1.verdi " +
                "FROM tekst t1 " +
                "WHERE t1.sprak = ? " +
                "AND t1.fra_dato = ( " +
                "    SELECT MAX(t2.fra_dato) " +
                "    FROM tekst t2 " +
                "    WHERE t2.sprak = t1.sprak " +
                "    AND t2.fra_dato <= TO_DATE(?, 'YYYY-MM-DD') " +
                "    AND t2.kode = t1.kode " +
                ") " +
                "ORDER BY t1.kode"
    )
        .use { preparedStatement ->
            preparedStatement.setString(1, sprak)
            preparedStatement.setString(2, checkDate(fraDato))

            preparedStatement.executeQuery()
                .use { resultSet ->
                    while (resultSet.next()) {
                        out[resultSet.getString("kode")] = clobToString(resultSet.getCharacterStream("verdi"))
                    }
                }
        }

    return out
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
private fun clobToString(reader: Reader?): String {
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

private fun checkDate(fraDato: String): String {
    var checkedDate = fraDato

    val pattern = "\\d{4}-\\d{2}-\\d{2}".toRegex()
    if (!pattern.matches(fraDato)) {
        checkedDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
        defaultLog.warn("Feil i fraDato. Fikk $fraDato")
    }

    return checkedDate
}