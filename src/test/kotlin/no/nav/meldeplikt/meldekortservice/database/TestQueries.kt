package no.nav.meldeplikt.meldekortservice.database

import no.nav.meldeplikt.meldekortservice.model.database.InnsendtMeldekort
import java.sql.Connection

fun Connection.slettAlleInnsendteMeldekort() =
    prepareStatement("""TRUNCATE TABLE INNSENDT_MELDEKORT""")
        .use { it.execute() }

fun Connection.slettInnsendtMeldekort(meldekortId: Long) =
    prepareStatement("""DELETE FROM INNSENDT_MELDEKORT WHERE meldekortId = ?""")
        .use {
            it.setLong(1, meldekortId)
            it.execute()
        }

fun Connection.hentAlleInnsendteMeldekort(): List<InnsendtMeldekort> =
    prepareStatement("""SELECT * FROM INNSENDT_MELDEKORT""")
        .use {
            it.executeQuery().list {
                tilInnsendtMeldekort()
            }
        }

fun Connection.hentJournalpostData(): List<Triple<Long, Long, Long>> =
    prepareStatement("""SELECT journalpostId, dokumentInfoId, meldekortId FROM OPPRETTEDE_JOURNALPOSTER""")
        .use {
            it.executeQuery().list {
                Triple(
                    this.getLong("journalpostId"),
                    this.getLong("dokumentInfoId"),
                    this.getLong("meldekortId")
                )
            }
        }