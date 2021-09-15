CREATE TABLE JOURNALPOST_MELDEKORT
(
    journalpostId numeric primary key,
    meldekortId   numeric not null,
    created       numeric not null
);

CREATE TABLE JOURNALPOST
(
    id          char(36) primary key,
    journalpost text not null,
    created     numeric not null,
    retries     numeric default 0 not null
);
