package no.nav.meldeplikt.meldekortservice.database

import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import java.sql.Connection
import java.sql.DatabaseMetaData

fun Connection.hentAlleKallLogg(): List<KallLogg> {
    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    // Oracle default
    var query = "SELECT " +
            "kall_logg_id, korrelasjon_id, tidspunkt, type, kall_retning, method, operation, status, kalltid, ident, " +
            "request, " +
            "response, " +
            "logginfo " +
            "FROM kall_logg " +
            "ORDER BY tidspunkt"
    if (productName == "PostgreSQL") {
        query = "SELECT " +
                "kall_logg_id, korrelasjon_id, tidspunkt, type, kall_retning, method, operation, status, kalltid, ident, " +
                "convert_from(lo_get(request::oid), 'UTF8') as request," +
                "convert_from(lo_get(response::oid), 'UTF8') as response," +
                "convert_from(lo_get(logginfo::oid), 'UTF8') as logginfo " +
                "FROM kall_logg " +
                "ORDER BY tidspunkt"
    }

    val list = mutableListOf<KallLogg>()
    this.prepareStatement(query)
        .use { preparedStatement ->
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
                                ident = resultSet.getString("ident"),
                            )
                        )
                    }
                }
        }

    return list
}
