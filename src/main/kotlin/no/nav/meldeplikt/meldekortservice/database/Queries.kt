package no.nav.meldeplikt.meldekortservice.database

import no.nav.meldeplikt.meldekortservice.model.database.Message
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getAllMessages(): List<Message> =
    prepareStatement("""SELECT * FROM TEST""")
        .use {
            it.executeQuery().list {
                toMessage()
            }
        }

private fun ResultSet.toMessage(): Message {
    return Message(
        id = getInt("id"),
        message = getString("message")
    )
}

private fun <T> ResultSet.list(result: ResultSet.() -> T): List<T> =
    mutableListOf<T>().apply {
        while (next()) {
            add(result())
        }
    }