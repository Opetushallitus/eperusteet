create table perusteenosaviite_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    perusteenosa_id int8,
    vanhempi_id int8,
    primary key (id, REV)
);

create table peruste_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    koodiUri varchar(255),
    paivays timestamp,
    siirtyma timestamp,
    tutkintokoodi varchar(255),
    nimi_id int8,
    primary key (id, REV)
);

create table peruste_koulutus_AUD (
    REV int4 not null,
    peruste_id int8 not null,
    koulutus_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, peruste_id, koulutus_id)
);

create table peruste_suoritustapa_AUD (
    REV int4 not null,
    peruste_id int8 not null,
    suoritustapa_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, peruste_id, suoritustapa_id)
);

create table suoritustapa_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    suoritustapakoodi varchar(255),
    tutkinnon_rakenne_id int8,
    sisalto_perusteenosaviite_id int8,
    primary key (id, REV)
);

alter table perusteenosaviite_AUD
    add constraint FK_perusteenosaviite_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table perusteenosaviite_AUD
    add constraint FK_perusteenosaviite_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table peruste_AUD
    add constraint FK_peruste_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table peruste_AUD
    add constraint FK_peruste_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table peruste_koulutus_AUD
    add constraint FK_peruste_koulutus_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table peruste_koulutus_AUD
    add constraint FK_peruste_koulutus_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table peruste_suoritustapa_AUD
    add constraint FK_peruste_suoritustapa_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table peruste_suoritustapa_AUD
    add constraint FK_peruste_suoritustapa_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table suoritustapa_AUD
    add constraint FK_suoritustapa_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table suoritustapa_AUD
    add constraint FK_suoritustapa_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;
