alter table arviointi
    drop column luoja,
    drop column luotu,
    drop column muokattu,
    drop column muokkaaja;

alter table arviointi_AUD
    drop column luoja,
    drop column luotu,
    drop column muokattu,
    drop column muokkaaja;

alter table tutkinnonosa_AUD
    drop column arviointi_MOD;

create table arvioinninkohde_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    arviointiAsteikko_id int8,
    otsikko_id int8,
    primary key (id, REV)
);

create table arvioinninkohde_osaamistasonkriteeri_AUD (
    REV int4 not null,
    arvioinninkohde_id int8 not null,
    osaamistasonKriteerit_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, arvioinninkohde_id, osaamistasonKriteerit_id)
);


create table arvioinninkohdealue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    otsikko_id int8,
    primary key (id, REV)
);

create table arvioinninkohdealue_arvioinninkohde_AUD (
    REV int4 not null,
    arvioinninkohdealue_id int8 not null,
    arvioinninkohde_id int8 not null,
    arvioinninKohteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, arvioinninkohdealue_id, arvioinninkohde_id, arvioinninKohteet_ORDER)
);

create table osaamistasonkriteeri_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    osaamistaso_id int8,
    primary key (id, REV)
);

create table osaamistasonkriteeri_tekstipalanen_AUD (
    REV int4 not null,
    osaamistasonkriteeri_id int8 not null,
    tekstipalanen_id int8 not null,
    kriteerit_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaamistasonkriteeri_id, tekstipalanen_id, kriteerit_ORDER)
);

alter table arvioinninkohde_AUD
    add constraint FK_arvioinninkohde_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table arvioinninkohde_AUD
    add constraint FK_arvioinninkohde_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table arvioinninkohde_osaamistasonkriteeri_AUD
    add constraint FK_arvioinninkohde_osaamistasonkriteeri_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table arvioinninkohde_osaamistasonkriteeri_AUD
    add constraint FK_arvioinninkohde_osaamistasonkriteeri_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table arvioinninkohdealue_AUD
    add constraint FK_arvioinninkohdealue_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table arvioinninkohdealue_AUD
    add constraint FK_arvioinninkohdealue_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table arvioinninkohdealue_arvioinninkohde_AUD
    add constraint FK_arvioinninkohdealue_arvioinninkohde_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table arvioinninkohdealue_arvioinninkohde_AUD
    add constraint FK_arvioinninkohdealue_arvioinninkohde_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table osaamistasonkriteeri_AUD 
    add constraint FK_osaamistasonkriteeri_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table osaamistasonkriteeri_AUD
    add constraint FK_osaamistasonkriteeri_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;

alter table osaamistasonkriteeri_tekstipalanen_AUD
    add constraint FK_osaamistasonkriteeri_tekstipalanen_AUD_REV_REVINFO
    foreign key (REV)
    references REVINFO;

alter table osaamistasonkriteeri_tekstipalanen_AUD
    add constraint FK_osaamistasonkriteeri_tekstipalanen_AUD_REVEND_REVINFO
    foreign key (REVEND)
    references REVINFO;


