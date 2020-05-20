create table yllapito (
    id int8 not null,
    ominaisuus varchar(255),
    sallittu boolean,
    url varchar(255),
    primary key (id)
);

create table yllapito_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    ominaisuus varchar(255),
    sallittu boolean,
    url varchar(255),
    primary key (id, REV)
);
