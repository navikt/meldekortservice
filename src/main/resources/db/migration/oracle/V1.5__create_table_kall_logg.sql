-------------------------------------------------------------------------------
-- Tabell            : KALL_LOGG
-- Beskrivelse       : Loggtabell for API-kall og Kafka hendelser.
-------------------------------------------------------------------------------
CREATE TABLE kall_logg
(
    kall_logg_id   NUMBER GENERATED ALWAYS AS IDENTITY,
    korrelasjon_id VARCHAR2(54)                           NOT NULL,
    tidspunkt      TIMESTAMP(9) DEFAULT current_timestamp NOT NULL,
    type           VARCHAR2(10)                           NOT NULL,
    kall_retning   VARCHAR2(10)                           NOT NULL,
    method         VARCHAR2(10),
    operation      VARCHAR2(100)                          NOT NULL,
    status         NUMBER,
    kalltid        NUMBER                                 NOT NULL,
    request        CLOB,
    response       CLOB,
    logginfo       CLOB,
    CONSTRAINT kalo_pk PRIMARY KEY (kall_logg_id)
)
    PARTITION BY RANGE (tidspunkt)
    INTERVAL (NUMTODSINTERVAL(1, 'day'))
(
    PARTITION P_INITIAL VALUES LESS THAN (TO_DATE('2022-10-01', 'YYYY-MM-DD'))
);
-- INTERVAL PARTITION skal oprette partisjoner automatisk når det er nødvendig

-- Indekser
CREATE INDEX kalo_1 ON kall_logg (operation, kall_retning);

CREATE INDEX kalo_2 ON kall_logg (korrelasjon_id);

CREATE INDEX kalo_3 ON kall_logg (status);

-- Constraints
ALTER TABLE kall_logg
    ADD CONSTRAINT type_ck1 CHECK ( type IN ('REST', 'KAFKA') );

ALTER TABLE kall_logg
    ADD CONSTRAINT kall_retning_ck1 CHECK ( kall_retning IN ('INN', 'UT') );

-- Tabell og kolonnekommentarer
COMMENT ON TABLE kall_logg IS 'Loggtabell for API-kall og Kafka hendelser.';

COMMENT ON COLUMN kall_logg.kall_logg_id IS 'Autogenerert sekvens';
COMMENT ON COLUMN kall_logg.korrelasjon_id IS 'Unik ID som kan brukes for å korrelere logginnslag med logging til Kibana.';
COMMENT ON COLUMN kall_logg.tidspunkt IS 'Tidspunkt for når kallet bli mottatt/utført.';
COMMENT ON COLUMN kall_logg.type IS 'Grensesnittype: REST/KAFKA';
COMMENT ON COLUMN kall_logg.kall_retning IS 'Kallretning: INN: API-kall til applikasjonen. UT: Kall til underliggende tjeneste eller hendelser ut på Kafka.';
COMMENT ON COLUMN kall_logg.method IS 'HTTP-metode. (GET, POST osv.)';
COMMENT ON COLUMN kall_logg.operation IS 'REST: Ressursstien (request URI) til kallet. KAFKA: Navn på Kafka-topic.';
COMMENT ON COLUMN kall_logg.status IS 'HTTP-statuskode returnert fra kallet. For Kafka-grensesnitt: N/A.';
COMMENT ON COLUMN kall_logg.kalltid IS 'Målt tid for utførelse av kallet i millisekunder.';
COMMENT ON COLUMN kall_logg.request IS 'Sendt Kafka-hendelse eller REST-kall.';
COMMENT ON COLUMN kall_logg.response IS 'Komplett HTTP-respons m/ status, headere og responsdata.';
COMMENT ON COLUMN kall_logg.logginfo IS 'Tilleggsinformasjon til fri bruk. Kan typisk brukes for feilmeldinger, stacktrace eller annet som kan være nyttig å logge.';
