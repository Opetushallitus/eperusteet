create table osaalue (
    id bigint not null,
    nimi_id bigint,
    primary key (id)
);

create table osaalue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    nimi_id int8,
    primary key (id, REV)
);

create table osaamistavoite (
    id int8 not null,
    laajuus numeric(10, 2),
    pakollinen boolean not null,
    arviointi_id int8,
    nimi_id int8,
    tavoitteet_id int8,
    tunnustaminen_id int8,
    primary key (id)
);

create table osaamistavoite_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    laajuus numeric(10, 2),
    pakollinen boolean,
    arviointi_id int8,
    nimi_id int8,
    tavoitteet_id int8,
    tunnustaminen_id int8,
    primary key (id, REV)
);

create table tutkinnonosa_osaalue (
    tutkinnonosa_id int8 REFERENCES tutkinnonosa(id),
    osaalue_id int8 REFERENCES osaalue(id)
);

create table tutkinnonosa_osaalue_AUD (
    REV int4 not null,
    tutkinnonosa_id int8 not null,
    osaalue_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tutkinnonosa_id, osaalue_id)
);

create table osaalue_osaamistavoite (
    osaalue_id int8 REFERENCES osaalue(id),
    osaamistavoite_id int8 REFERENCES osaamistavoite(id)
);

create table osaalue_osaamistavoite_AUD (
    REV int4 not null,
    osaalue_id int8 not null,
    osaamistavoite_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaalue_id, osaamistavoite_id)
);

alter table only osaalue 
    add constraint FK_osaalue_nimi_tekstipalanen 
    foreign key (nimi_id) 
    references tekstipalanen;

alter table osaalue_AUD 
    add constraint FK_osaalue_AUD_REVINFO_REV 
    foreign key (REV) 
    references revinfo;

alter table osaalue_AUD 
    add constraint FK_osaalue_AUD_REVINFO_REVEND
    foreign key (REVEND) 
    references revinfo;

alter table osaalue_osaamistavoite_AUD 
    add constraint FK_osaalue_osaamistavoite_AUD_REVINFO_REV 
    foreign key (REV) 
    references revinfo;

alter table osaalue_osaamistavoite_AUD 
    add constraint FK_osaalue_osaamistavoite_AUD_REVINFO_REVEND 
    foreign key (REVEND) 
    references revinfo;

alter table tutkinnonosa_osaalue_AUD 
    add constraint FK_tutkinnonosa_osaalue_AUD_REVINFO_REV 
    foreign key (REV) 
    references revinfo;

alter table tutkinnonosa_osaalue_AUD 
    add constraint FK_tutkinnonosa_osaalue_AUD_REVINFO_REVEND
    foreign key (REVEND) 
    references revinfo;

alter table osaamistavoite 
    add constraint FK_osaamistavoite_arviointi
    foreign key (arviointi_id) 
    references arviointi;

alter table osaamistavoite 
    add constraint FK_osaamistavoite_nimi_tekstipalanen
    foreign key (nimi_id) 
    references tekstipalanen;

alter table osaamistavoite 
    add constraint FK_osaamistavoite_tavoitteet_tekstipalanen
    foreign key (tavoitteet_id) 
    references tekstipalanen;

alter table osaamistavoite 
    add constraint FK_osaamistavoite_tunnustaminen_tekstipalanen
    foreign key (tunnustaminen_id) 
    references tekstipalanen;

alter table osaamistavoite_AUD 
    add constraint FK_osaamistavoite_AUD_REVINFO_REV
    foreign key (REV) 
    references revinfo;

alter table osaamistavoite_AUD 
    add constraint FK_osaamistavoite_AUD_REVINFO_REVEND
    foreign key (REVEND) 
    references revinfo;