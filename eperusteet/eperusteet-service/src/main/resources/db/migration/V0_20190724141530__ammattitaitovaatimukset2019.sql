create table ammattitaitovaatimus2019kohdealue (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kuvaus_id int8 references tekstipalanen(id),
    primary key (id)
);

create table ammattitaitovaatimus2019kohdealue_aud (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kuvaus_id int8,
    primary key (id)
);

create table ammattitaitovaatimus2019 (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodi_id int8 references koodi(id),
    vaatimus_id int8 references koodi(id),
    kohdealue_id int8 references tekstipalanen(id),
    primary key (id)
);

create table ammattitaitovaatimus2019_aud (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodi_id int8,
    vaatimus_id int8,
    kohdealue_id int8,
    primary key (id)
);

create table ammattitaitovaatimukset2019 (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kohde_id int8 references tekstipalanen(id),
    primary key (id)
);

create table ammattitaitovaatimukset2019_aud  (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kohde_id int8,
    primary key (id, REV)
);

create table ammattitaitovaatimus2019_kohdealue (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kuvaus_id int8 references tekstipalanen(id),
    primary key (id)
);

create table ammattitaitovaatimus2019_kohdealue_aud (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kuvaus_id int8 references tekstipalanen(id),
    primary key (id, REV)
);

create table ammattitaitovaatimukset2019_ammattitaitovaatimus2019 (
    ammattitaitovaatimukset_id int8 not null references ammattitaitovaatimukset2019(id),
    ammattitaitovaatimus_id int8 not null references ammattitaitovaatimus2019(id),
    primary key (ammattitaitovaatimukset_id, ammattitaitovaatimus_id)
);

create table ammattitaitovaatimukset2019_ammattitaitovaatimus2019_aud (
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    ammattitaitovaatimukset_id int8 not null references ammattitaitovaatimukset2019(id),
    ammattitaitovaatimus_id int8 not null references ammattitaitovaatimus2019(id),
    primary key (REV, ammattitaitovaatimukset_id, ammattitaitovaatimus_id)
);

alter table tutkinnonosa add column ammattitaitovaatimukset2019_id int8 references ammattitaitovaatimukset2019(id);
alter table tutkinnonosa_aud add column ammattitaitovaatimukset2019_id int8;
