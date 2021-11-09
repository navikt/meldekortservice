CREATE TABLE opprettede_journalposter
(
    journalpostId  numeric primary key,
    dokumentInfoId numeric                             not null,
    meldekortId    numeric                             not null,
    created        timestamp default current_timestamp not null
);

CREATE INDEX opprettede_journalposter_meldekortid_index ON opprettede_journalposter (meldekortid);

CREATE TABLE midlertidig_lagrede_journalposter
(
    id          char(36) primary key,
    journalpost clob                                not null,
    created     timestamp default current_timestamp not null,
    retries     numeric   default 0                 not null
);
