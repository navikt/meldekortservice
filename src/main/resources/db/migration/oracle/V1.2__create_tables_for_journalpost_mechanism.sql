CREATE TABLE opprettede_journalposter
(
    journalpostId  numeric primary key,
    dokumentInfoId numeric                                                              not null,
    meldekortId    numeric                                                              not null,
    created        char(19) default to_char(current_timestamp, 'YYYY-MM-DD HH24:MI:SS') not null
);

CREATE INDEX opprettede_journalposter_meldekortid_index ON opprettede_journalposter (meldekortid);

CREATE TABLE midlertidig_lagrede_journalposter
(
    id          char(36) primary key,
    journalpost clob                                                                 not null,
    created     char(19) default to_char(current_timestamp, 'YYYY-MM-DD HH24:MI:SS') not null,
    retries     numeric  default 0                                                   not null
);
