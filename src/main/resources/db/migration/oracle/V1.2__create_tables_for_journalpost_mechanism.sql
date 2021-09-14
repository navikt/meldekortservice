CREATE TABLE JOURNALPOST_MELDEKORT
(
    journalpostId numeric primary key,
    meldekortId   numeric,
    created       numeric
);

CREATE TABLE JOURNALPOST
(
    id          char(36) primary key,
    journalpost clob,
    created     numeric,
    retries     numeric
);
