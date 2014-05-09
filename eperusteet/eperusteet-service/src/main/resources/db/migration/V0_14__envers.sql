create table REVINFO (
    REV int4 not null,
    REVTSTMP int8,
    primary key (REV)
);

alter table perusteenosa
    add column luoja varchar(255),
    add column muokkaaja varchar(255);

create table perusteenosa_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    primary key (id, REV)
);

create table tekstikappale_AUD (
    id int8 not null,
    REV int4 not null,
    teksti_id int8,
    primary key (id, REV)
);

create table tutkinnonosa_AUD (
    id int8 not null,
    REV int4 not null,
    koodi int8,
    opintoluokitus int8,
    ammattitaidonOsoittamistavat_id int8,
    ammattitaitovaatimukset_id int8,
    arviointi_id int8,
    osaamisala_id int8,
    tavoitteet_id int8,
    primary key (id, REV)
);

alter table perusteenosa_AUD
    add constraint FK_perusteenosa_AUD_REVINFO_REV
    foreign key (REV)
    references REVINFO;

alter table perusteenosa_AUD
    add constraint FK_perusteenosa_AUD_REVINFO_REVEND
    foreign key (REVEND)
    references REVINFO;

alter table tekstikappale_AUD
    add constraint FK_tekstikappale_AUD_perusteenosa_AUD_id_REV
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table tutkinnonosa_AUD
    add constraint FK_tutkinnonosa_AUD_perusteenosa_AUD_id_REV
    foreign key (id, REV)
    references perusteenosa_AUD;
