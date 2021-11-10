package no.nav.meldeplikt.meldekortservice.database

import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
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

fun Connection.hentAlleInnsendteMeldekort(): List<InnsendtMeldekort> =
    prepareStatement("SELECT * FROM innsendt_meldekort")
        .use {
            it.executeQuery().list {
                tilInnsendtMeldekort()
            }
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