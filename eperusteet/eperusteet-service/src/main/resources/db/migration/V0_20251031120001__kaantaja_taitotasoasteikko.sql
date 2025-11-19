-- KaantajaTaitotasoasteikko main table (extends perusteenosa)
create table kaantaja_taitotasoasteikko (
    id int8 not null,
    kuvaus_id int8,
    primary key (id)
);

-- KaantajaTaitotasoasteikko audit table
create table kaantaja_taitotasoasteikko_AUD (
    id int8 not null,
    REV int4 not null,
    kuvaus_id int8,
    primary key (id, REV)
);

-- TaitotasoasteikkoKategoria main table
create table taitotasoasteikko_kategoria (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    otsikko_id int8,
    kaantajaTaitotasoasteikko_id int8,
    taitotasoasteikkoKategoriat_ORDER int4,
    primary key (id)
);

-- TaitotasoasteikkoKategoria audit table
create table taitotasoasteikko_kategoria_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    otsikko_id int8,
    kaantajaTaitotasoasteikko_id int8,
    taitotasoasteikkoKategoriat_ORDER int4,
    primary key (id, REV)
);

-- TaitotasoasteikkoKategoriaTaitotaso main table
create table taitotasoasteikko_kategoria_taitotaso (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    otsikko_id int8,
    kuvaus_id int8,
    taitotasoasteikkoKategoria_id int8,
    taitotasoasteikkoKategoriaTaitotasot_ORDER int4,
    primary key (id)
);

-- TaitotasoasteikkoKategoriaTaitotaso audit table
create table taitotasoasteikko_kategoria_taitotaso_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    otsikko_id int8,
    kuvaus_id int8,
    taitotasoasteikkoKategoria_id int8,
    taitotasoasteikkoKategoriaTaitotasot_ORDER int4,
    primary key (id, REV)
);

-- Foreign key constraints for kaantaja_taitotasoasteikko
alter table kaantaja_taitotasoasteikko
    add constraint FK_kaantaja_taitotasoasteikko_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_taitotasoasteikko
    add constraint FK_kaantaja_taitotasoasteikko_perusteenosa
    foreign key (id)
    references perusteenosa;

-- Foreign key constraints for kaantaja_taitotasoasteikko audit
alter table kaantaja_taitotasoasteikko_AUD
    add constraint FK_kaantaja_taitotasoasteikko_AUD_perusteenosa_AUD
    foreign key (id, REV)
    references perusteenosa_AUD;

-- Foreign key constraints for taitotasoasteikko_kategoria
alter table taitotasoasteikko_kategoria
    add constraint FK_taitotasoasteikko_kategoria_otsikko
    foreign key (otsikko_id)
    references tekstipalanen;

alter table taitotasoasteikko_kategoria
    add constraint FK_taitotasoasteikko_kategoria_kaantaja_taitotasoasteikko
    foreign key (kaantajaTaitotasoasteikko_id)
    references kaantaja_taitotasoasteikko;

-- Foreign key constraints for taitotasoasteikko_kategoria audit
alter table taitotasoasteikko_kategoria_AUD
    add constraint FK_taitotasoasteikko_kategoria_AUD_REV
    foreign key (REV)
    references revinfo;

alter table taitotasoasteikko_kategoria_AUD
    add constraint FK_taitotasoasteikko_kategoria_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for taitotasoasteikko_kategoria_taitotaso
alter table taitotasoasteikko_kategoria_taitotaso
    add constraint FK_taitotasoasteikko_kategoria_taitotaso_otsikko
    foreign key (otsikko_id)
    references tekstipalanen;

alter table taitotasoasteikko_kategoria_taitotaso
    add constraint FK_taitotasoasteikko_kategoria_taitotaso_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table taitotasoasteikko_kategoria_taitotaso
    add constraint FK_taitotasoasteikko_kategoria_taitotaso_kategoria
    foreign key (taitotasoasteikkoKategoria_id)
    references taitotasoasteikko_kategoria;

-- Foreign key constraints for taitotasoasteikko_kategoria_taitotaso audit
alter table taitotasoasteikko_kategoria_taitotaso_AUD
    add constraint FK_taitotasoasteikko_kategoria_taitotaso_AUD_REV
    foreign key (REV)
    references revinfo;

alter table taitotasoasteikko_kategoria_taitotaso_AUD
    add constraint FK_taitotasoasteikko_kategoria_taitotaso_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

