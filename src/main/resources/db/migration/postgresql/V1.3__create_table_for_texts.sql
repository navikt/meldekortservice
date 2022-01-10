create table tekst
(
    kode          varchar(255) not null,
    verdi         text,
    sprak         varchar(2)   not null,
    fra_tidspunkt varchar(19)  not null,
    PRIMARY KEY (kode, sprak, fra_tidspunkt)
);