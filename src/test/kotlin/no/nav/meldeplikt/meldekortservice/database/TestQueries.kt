package no.nav.meldeplikt.meldekortservice.database

import no.nav.meldeplikt.meldekortservice.model.database.KallLogg
import java.sql.Connection
import java.sql.DatabaseMetaData

fun Connection.slettAlleInnsendteMeldekort() =
    prepareStatement("TRUNCATE TABLE innsendt_meldekort")
        .use { it.execute() }

fun Connection.slettInnsendtMeldekort(meldekortId: Long) =
    prepareStatement("DELETE FROM innsendt_meldekort WHERE meldekortId = ?")
        .use {
            it.setLong(1, meldekortId)
            it.execute()
        }

fun Connection.hentJournalpostData(): List<Triple<Long, Long, Long>> =
    prepareStatement("SELECT journalpostId, dokumentInfoId, meldekortId FROM opprettede_journalposter")
        .use {
            it.executeQuery().list {
                Triple(
                    this.getLong("journalpostId"),
                    this.getLong("dokumentInfoId"),
                    this.getLong("meldekortId")
                )
            }
        }

fun Connection.hentAlleMidlertidigLagredeJournalposter(): List<Pair<String, Int>> {
    val list = mutableListOf<Pair<String, Int>>()

    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    // Oracle and H2 by default
    var query = "SELECT id, retries FROM midlertidig_lagrede_journalposter"
    if (productName == "PostgreSQL") {
        query = "SELECT id, retries FROM midlertidig_lagrede_journalposter"
    }

    this.prepareStatement(query)
        .use { preparedStatement ->
            preparedStatement.executeQuery()
                .use { resultSet ->
                    while (resultSet.next()) {
                        list.add(
                            Pair(
                                resultSet.getString("id"),
                                resultSet.getInt("retries")
                            )
                        )
                    }
                }
        }

    return list
}

fun Connection.hentAlleKallLogg(): List<KallLogg> {
    val metaData: DatabaseMetaData = this.metaData
    val productName = metaData.databaseProductName

    // Oracle default
    var query = "SELECT " +
            "kall_logg_id, korrelasjon_id, tidspunkt, type, kall_retning, method, operation, status, kalltid, " +
            "request, " +
            "response, " +
            "logginfo " +
            "FROM kall_logg " +
            "ORDER BY tidspunkt"
    if (productName == "PostgreSQL") {
        query = "SELECT " +
                "kall_logg_id, korrelasjon_id, tidspunkt, type, kall_retning, method, operation, status, kalltid, " +
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
                                logginfo = clobToString(resultSet.getCharacterStream("logginfo"))
                            )
                        )
                    }
                }
        }

    return list
}
