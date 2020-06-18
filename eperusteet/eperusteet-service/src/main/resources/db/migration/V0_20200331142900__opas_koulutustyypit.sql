create table opas_koulutustyyppi (
    opas_id int8 not null,
    koulutustyyppi varchar(255)
);

create table opas_koulutustyyppi_AUD (
    REV int4 not null,
    opas_id int8 not null,
    koulutustyyppi varchar(255) not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, opas_id, koulutustyyppi)
);

create table opas_peruste (
    opas_id int8 not null,
    peruste_id int8 not null,
    primary key (opas_id, peruste_id)
);

create table opas_peruste_AUD (
    REV int4 not null,
    opas_id int8 not null,
    peruste_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, opas_id, peruste_id)
);
