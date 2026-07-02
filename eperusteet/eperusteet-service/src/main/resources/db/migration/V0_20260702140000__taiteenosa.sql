create table taiteenosa (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    laajuus numeric(10, 2),
    nimi_id int8,
    kuvaus_id int8,
    taiteenala_id int8,
    taiteenOsat_ORDER int4,
    primary key (id)
);

create table taiteenosa_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    laajuus numeric(10, 2),
    nimi_id int8,
    kuvaus_id int8,
    taiteenala_id int8,
    taiteenOsat_ORDER int4,
    primary key (id, REV)
);

create table taiteenosa_tavoitteet (
    taiteenosa_id int8 not null,
    tavoite_id int8 not null,
    tavoitteet_ORDER int4 not null,
    primary key (taiteenosa_id, tavoitteet_ORDER)
);

create table taiteenosa_tavoitteet_AUD (
    REV int4 not null,
    taiteenosa_id int8 not null,
    tavoite_id int8 not null,
    tavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, taiteenosa_id, tavoite_id, tavoitteet_ORDER)
);

alter table taiteenosa
    add constraint FK_taiteenosa_nimi
    foreign key (nimi_id)
    references tekstipalanen;

alter table taiteenosa
    add constraint FK_taiteenosa_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table taiteenosa
    add constraint FK_taiteenosa_taiteenala
    foreign key (taiteenala_id)
    references taiteenala;

alter table taiteenosa_tavoitteet
    add constraint FK_taiteenosa_tavoitteet_taiteenosa
    foreign key (taiteenosa_id)
    references taiteenosa;

alter table taiteenosa_tavoitteet
    add constraint FK_taiteenosa_tavoitteet_tavoite
    foreign key (tavoite_id)
    references tekstipalanen;

alter table taiteenosa_AUD
    add constraint FK_taiteenosa_AUD_REV
    foreign key (REV)
    references revinfo;

alter table taiteenosa_AUD
    add constraint FK_taiteenosa_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

alter table taiteenosa_tavoitteet_AUD
    add constraint FK_taiteenosa_tavoitteet_AUD_REV
    foreign key (REV)
    references revinfo;

alter table taiteenosa_tavoitteet_AUD
    add constraint FK_taiteenosa_tavoitteet_AUD_REVEND
    foreign key (REVEND)
    references revinfo;
