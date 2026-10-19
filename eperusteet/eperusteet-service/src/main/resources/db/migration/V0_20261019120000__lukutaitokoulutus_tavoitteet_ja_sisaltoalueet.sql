create table lukutaitokoulutus_tjs (
    id int8 not null,
    primary key (id)
);

create table lukutaitokoulutus_tjs_AUD (
    id int8 not null,
    REV int4 not null,
    primary key (id, REV)
);

create table lukutaitokoulutus_tavoitealue (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    tjs_id int8,
    tavoitealueet_ORDER int4,
    primary key (id)
);

create table lukutaitokoulutus_tavoitealue_AUD (
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

create table lukutaitokoulutus_tavoitealue_tavoitteet (
    tavoitealue_id int8 not null,
    tavoite_id int8 not null,
    tavoitteet_ORDER int4 not null,
    primary key (tavoitealue_id, tavoitteet_ORDER)
);

alter table lukutaitokoulutus_tjs
    add constraint FK_lukutaitokoulutus_tjs_perusteenosa
    foreign key (id)
    references perusteenosa;

alter table lukutaitokoulutus_tjs_AUD
    add constraint FK_lukutaitokoulutus_tjs_AUD_perusteenosa_AUD
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table lukutaitokoulutus_tavoitealue
    add constraint FK_lukutaitokoulutus_tavoitealue_nimi
    foreign key (nimi_id)
    references tekstipalanen;

alter table lukutaitokoulutus_tavoitealue
    add constraint FK_lukutaitokoulutus_tavoitealue_tjs
    foreign key (tjs_id)
    references lukutaitokoulutus_tjs;

alter table lukutaitokoulutus_tavoitealue_AUD
    add constraint FK_lukutaitokoulutus_tavoitealue_AUD_REV
    foreign key (REV)
    references revinfo;

alter table lukutaitokoulutus_tavoitealue_AUD
    add constraint FK_lukutaitokoulutus_tavoitealue_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

alter table lukutaitokoulutus_tavoitealue_tavoitteet
    add constraint FK_lukutaitokoulutus_tavoitealue_tavoitteet_tavoitealue
    foreign key (tavoitealue_id)
    references lukutaitokoulutus_tavoitealue;

alter table lukutaitokoulutus_tavoitealue_tavoitteet
    add constraint FK_lukutaitokoulutus_tavoitealue_tavoitteet_tavoite
    foreign key (tavoite_id)
    references tekstipalanen;
