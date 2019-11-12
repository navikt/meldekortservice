package no.nav.meldeplikt.meldekortservice.database

import no.nav.meldeplikt.meldekortservice.model.database.Message
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

fun Connection.getAllMessages(): List<Message> =
    prepareStatement("""SELECT * FROM TEST""")
        .use {
            it.executeQuery().list {
                toMessage()
            }
        }

fun Connection.createMessage(message: Message): Int =
    prepareStatement("""INSERT INTO TEST (message) VALUES (?)""", Statement.RETURN_GENERATED_KEYS).use {
        it.setString(1, message.message)
        it.executeUpdate()
        it.generatedKeys.next()
        it.generatedKeys.getInt("id")
    }

private fun ResultSet.toMessage(): Message {
    return Message(
        id = getInt("id"),
        message = getString("message")
    )
}