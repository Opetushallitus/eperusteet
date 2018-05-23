create table validointi_status (
    id int8 not null,
    aikaleima timestamp not null,
    vaihtoOk boolean not null,
    peruste_id int8 not null unique references peruste(id),
    primary key (id)
);

create table validointi_status_info_validointi (
    id int8 not null primary key
);

create table validointi_status_info (
    id int8 not null,
    validointi_id int8 references validointi_status_info_validointi(id),
    suoritustapa varchar(255),
    viesti varchar(255),
    primary key (id)
);

create table validointi_status_validointi_status_info (
    validointi_status_id int8 not null references validointi_status(id),
    infot_id int8 not null references validointi_status_info(id)
);

create table validointi_status_info_tekstipalanen (
    validointi_status_info_id int8 not null references validointi_status_info(id),
    nimet_id int8 not null references tekstipalanen(id)
);

create table validointi_rakenneongelma (
    id int8 not null,
    ongelma varchar(255),
    ryhma_id int8 references tekstipalanen(id),
    primary key (id)
);

create table validointi_status_info_validointi_validointi_rakenneongelma (
    validointi_status_info_validointi_id int8,
    ongelmat_id int8 not null references validointi_rakenneongelma(id)
);
