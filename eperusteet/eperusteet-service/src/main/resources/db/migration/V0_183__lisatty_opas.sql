create table opas_sisalto (
    id bigint not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id bigint not null references peruste(id),
    sisalto_id bigint references perusteenosaviite(id),
    primary key (id)
);

create table opas_sisalto_AUD (
    id bigint not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id bigint,
    sisalto_id bigint,
    primary key (id, REV)
);
