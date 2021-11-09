CREATE TABLE OPPRETTEDE_JOURNALPOSTER
(
    journalpostId  numeric  primary key,
    dokumentInfoId numeric  not null,
    meldekortId    numeric  not null,
    created        char(19) not null
);

CREATE TABLE MIDLERTIDIG_LAGREDE_JOURNALPOSTER
(
    id          char(36) primary key,
    journalpost clob              not null,
    created     char(19)          not null,
    retries     numeric default 0 not null
);
