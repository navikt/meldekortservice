-------------------------------------------------------------------------------
-- Tabell            : KALL_LOGG
-- Beskrivelse       : Loggtabell for API-kall og Kafka hendelser.
-------------------------------------------------------------------------------
CREATE TABLE kall_logg
(
    kall_logg_id   SERIAL PRIMARY KEY,
    korrelasjon_id VARCHAR(54)                            NOT NULL,
    tidspunkt      TIMESTAMP(9) DEFAULT current_timestamp NOT NULL,
    type           VARCHAR(10)                            NOT NULL,
    kall_retning   VARCHAR(10)                            NOT NULL,
    method         VARCHAR(10),
    operation      VARCHAR(100)                           NOT NULL,
    status         INTEGER,
    kalltid        BIGINT                                 NOT NULL,
    request        TEXT,
    response       TEXT,
    logginfo       TEXT

);

-- Indekser
CREATE INDEX kalo_1 ON kall_logg (operation, kall_retning);

CREATE INDEX kalo_2 ON kall_logg (korrelasjon_id);

CREATE INDEX kalo_3 ON kall_logg (status);

-- Constraints
ALTER TABLE kall_logg
    ADD CONSTRAINT type_ck1 CHECK ( type IN ('REST', 'KAFKA') );

ALTER TABLE kall_logg
    ADD CONSTRAINT kall_retning_ck1 CHECK ( kall_retning IN ('INN', 'UT') );
