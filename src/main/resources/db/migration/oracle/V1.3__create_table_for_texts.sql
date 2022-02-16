create table tekst
(
    kode     varchar2(255) not null,
    verdi    clob,
    sprak    varchar2(2)   not null,
    fra_dato date          not null,
    PRIMARY KEY (kode, sprak, fra_dato)
);