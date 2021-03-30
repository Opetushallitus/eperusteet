drop table if exists tavoitealue_tavoitteet;
drop table if exists tavoitealue_tavoitteet_AUD;
drop table if exists tavoitealue_keskeiset_sisaltoalueet;
drop table if exists tavoitealue_keskeiset_sisaltoalueet_AUD;
drop table if exists tavoitealue;
drop table if exists tavoitealue_AUD;
drop table if exists tavoitesisaltoalue_tavoitealueet;
drop table if exists tavoitesisaltoalue_tavoitealueet_AUD;
drop table if exists tavoitesisaltoalue;
drop table if exists tavoitesisaltoalue_AUD;

create table tavoitealue (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tavoiteAlueTyyppi varchar(255),
    otsikko_id int8,
    primary key (id)
);

create table tavoitealue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tavoiteAlueTyyppi varchar(255),
    otsikko_id int8,
    primary key (id, REV)
);

create table tavoitealue_keskeiset_sisaltoalueet (
    tavoitealue_id int8 not null,
    keskeinen_sisaltoalue_id int8 not null,
    keskeisetSisaltoalueet_ORDER int4 not null,
    primary key (tavoitealue_id, keskeisetSisaltoalueet_ORDER)
);

create table tavoitealue_keskeiset_sisaltoalueet_AUD (
    REV int4 not null,
    tavoitealue_id int8 not null,
    keskeinen_sisaltoalue_id int8 not null,
    keskeisetSisaltoalueet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tavoitealue_id, keskeinen_sisaltoalue_id, keskeisetSisaltoalueet_ORDER)
);

create table tavoitealue_tavoitteet (
    tavoitealue_id int8 not null,
    tavoite_koodi_id int8 not null,
    tavoitteet_ORDER int4 not null,
    primary key (tavoitealue_id, tavoitteet_ORDER)
);

create table tavoitealue_tavoitteet_AUD (
    REV int4 not null,
    tavoitealue_id int8 not null,
    tavoite_koodi_id int8 not null,
    tavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tavoitealue_id, tavoite_koodi_id, tavoitteet_ORDER)
);

create table tavoitesisaltoalue (
    id int8 not null,
    nimiKoodi_id int8,
    teksti_id int8,
    primary key (id)
);

create table tavoitesisaltoalue_AUD (
    id int8 not null,
    REV int4 not null,
    nimiKoodi_id int8,
    teksti_id int8,
    primary key (id, REV)
);

create table tavoitesisaltoalue_tavoitealueet (
    tavoitesisaltoalue_id int8 not null,
    tavoitealue_id int8 not null,
    tavoitealueet_ORDER int4 not null,
    primary key (tavoitesisaltoalue_id, tavoitealueet_ORDER)
);

create table tavoitesisaltoalue_tavoitealueet_AUD (
    REV int4 not null,
    tavoitesisaltoalue_id int8 not null,
    tavoitealue_id int8 not null,
    tavoitealueet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tavoitesisaltoalue_id, tavoitealue_id, tavoitealueet_ORDER)
);

alter table tavoitealue_tavoitteet
    add constraint UK_1eyqyxhbsou95xk0uxddn6e9r  unique (tavoite_koodi_id);

alter table tavoitesisaltoalue_tavoitealueet
    add constraint UK_fayt8q5kbcirqtyhasrmcrrdv  unique (tavoitealue_id);

alter table tavoitealue
    add constraint FK_3x413vhqfu6c2tb1msj294wbv
    foreign key (otsikko_id)
    references koodi;

alter table tavoitealue_AUD
    add constraint FK_mewvbsyboka6haqpd8ndig516
    foreign key (REV)
    references revinfo;

alter table tavoitealue_AUD
    add constraint FK_elll1w7r3gm4i2a9n1kfmvf2g
    foreign key (REVEND)
    references revinfo;

alter table tavoitealue_keskeiset_sisaltoalueet
    add constraint FK_t6iqs3ptruxgan5092yk4svsg
    foreign key (keskeinen_sisaltoalue_id)
    references tekstipalanen;

alter table tavoitealue_keskeiset_sisaltoalueet
    add constraint FK_i4vcqpwwin8krvc7tm9j819wi
    foreign key (tavoitealue_id)
    references tavoitealue;

alter table tavoitealue_keskeiset_sisaltoalueet_AUD
    add constraint FK_1f3f80hlp2n2ki13c9nip99y7
    foreign key (REV)
    references revinfo;

alter table tavoitealue_keskeiset_sisaltoalueet_AUD
    add constraint FK_fl2xi00wu54sbvj47cggig6o9
    foreign key (REVEND)
    references revinfo;

alter table tavoitealue_tavoitteet
    add constraint FK_1eyqyxhbsou95xk0uxddn6e9r
    foreign key (tavoite_koodi_id)
    references koodi;

alter table tavoitealue_tavoitteet
    add constraint FK_ik90nu3eew3585muyc3q5gtq6
    foreign key (tavoitealue_id)
    references tavoitealue;

alter table tavoitealue_tavoitteet_AUD
    add constraint FK_41q06wcvxohj05lw86kjn7u32
    foreign key (REV)
    references revinfo;

alter table tavoitealue_tavoitteet_AUD
    add constraint FK_3k56rim4ss09k7tm2i4rb8mmp
    foreign key (REVEND)
    references revinfo;

alter table tavoitesisaltoalue
    add constraint FK_hep9srbbclmlg542jr28xv95a
    foreign key (nimiKoodi_id)
    references koodi;

alter table tavoitesisaltoalue
    add constraint FK_eyh82re9pnlkan5am6hpko2je
    foreign key (teksti_id)
    references tekstipalanen;

alter table tavoitesisaltoalue
    add constraint FK_jvrlqorf5tsx6a9e6bnmpbqyv
    foreign key (id)
    references perusteenosa;

alter table tavoitesisaltoalue_AUD
    add constraint FK_7s9814w8w0iv0rjsixxnie6mj
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table tavoitesisaltoalue_tavoitealueet
    add constraint FK_fayt8q5kbcirqtyhasrmcrrdv
    foreign key (tavoitealue_id)
    references tavoitealue;

alter table tavoitesisaltoalue_tavoitealueet
    add constraint FK_4gmkbxkf5ou9ekxxjhfa90v69
    foreign key (tavoitesisaltoalue_id)
    references tavoitesisaltoalue;

alter table tavoitesisaltoalue_tavoitealueet_AUD
    add constraint FK_rwy2r7tggew007lbf44dyq7qb
    foreign key (REV)
    references revinfo;

alter table tavoitesisaltoalue_tavoitealueet_AUD
    add constraint FK_bhqkuhluo3bi118varc8l6hmt
    foreign key (REVEND)
    references revinfo;
