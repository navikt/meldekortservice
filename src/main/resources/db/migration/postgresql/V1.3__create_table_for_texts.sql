create table texts
(
    key          varchar(255) not null,
    value        text,
    language     char(2)      not null,
    fromDateTime char(19)     not null
);