create table tekst
(
    kode     varchar(255) not null,
    verdi    text,
    sprak    varchar(2)   not null,
    fra_dato date         not null,
    PRIMARY KEY (kode, sprak, fra_dato)
);