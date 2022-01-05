create table texts
(
    key          varchar2(255) not null,
    value        clob,
    language     char(2)       not null,
    fromDateTime char(19)      not null
);