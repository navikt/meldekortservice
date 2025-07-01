package no.nav.meldeplikt.meldekortservice.database

import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import java.io.Reader
import java.io.Writer
import java.sql.Clob
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.Timestamp
import java.time.Instant
import javax.sql.rowset.serial.SerialClob


fun Connection.lagreKallLogg(kallLogg: KallLogg): Long {
    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    val sql = "INSERT INTO kall_logg " +
            "(korrelasjon_id, type, tidspunkt, kall_retning, method, operation, status, kalltid, request, response, logginfo, ident) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "

    prepareStatement(sql, arrayOf("kall_logg_id"))
        .use {
            val requestClob: Clob
            val responseClob: Clob?
            val logginfoClob: Clob

            if (productName == "PostgreSQL" || productName == "H2") {
                requestClob = SerialClob(kallLogg.request.toCharArray())

                responseClob = SerialClob(kallLogg.response.toCharArray())

                logginfoClob = SerialClob(kallLogg.logginfo.toCharArray())
            } else {
                requestClob = this.createClob()
                val requestWriter: Writer = requestClob.setCharacterStream(1L)
                requestWriter.write(kallLogg.request)
                requestWriter.flush()
                requestWriter.close()

                responseClob = this.createClob()
                val responseWriter: Writer = responseClob.setCharacterStream(1L)
                responseWriter.write(kallLogg.response)
                responseWriter.flush()
                responseWriter.close()


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
            it.setString(12, kallLogg.ident)

            it.executeUpdate()

            var kallLoggId = 0L
            it.generatedKeys.use { keys ->
                if (keys.next()) {
                    kallLoggId = keys.getLong(1) // Can't refer to the returned keys by name in Oracle
                }
            }

            return kallLoggId
        }
}

fun Connection.lagreResponse(kallLoggId: Long, status: Int, response: String) {
    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    prepareStatement(
        "UPDATE kall_logg " +
                "SET response = ?, status = ?, kalltid = (? - kalltid) " +
                "WHERE kall_logg_id = ?"
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
            it.setLong(4, kallLoggId)

            it.executeUpdate()
        }
}

fun Connection.hentKallLoggFelterListeByKorrelasjonId(korrelasjonId: String): List<KallLogg> {
    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    // Oracle default
    var query = "SELECT " +
            "kall_logg_id, korrelasjon_id, tidspunkt, type, kall_retning, method, operation, status, kalltid, ident, " +
            "request, " +
            "response, " +
            "logginfo " +
            "FROM kall_logg " +
            "WHERE korrelasjon_id = ?"
    if (productName == "PostgreSQL") {
        query = "SELECT " +
                "kall_logg_id, korrelasjon_id, tidspunkt, type, kall_retning, method, operation, status, kalltid, ident, " +
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
                                logginfo = clobToString(resultSet.getCharacterStream("logginfo")),
                                ident =  resultSet.getString("ident")
                            )
                        )
                    }
                }
        }

    return list
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
