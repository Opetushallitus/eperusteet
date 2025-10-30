-- KaantajaTaito main table (extends perusteenosa)
create table kaantaja_taito (
    id int8 not null,
    kuvaus_id int8,
    valiotsikko_id int8,
    primary key (id)
);

-- KaantajaTaito audit table
create table kaantaja_taito_AUD (
    id int8 not null,
    REV int4 not null,
    kuvaus_id int8,
    valiotsikko_id int8,
    primary key (id, REV)
);

-- KaantajaTaitoKohdealue main table
create table kaantaja_taito_kohdealue (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kohdealueOtsikko_id int8,
    kaantajaTaito_id int8,
    kohdealueet_ORDER int4,
    primary key (id)
);

-- KaantajaTaitoKohdealue audit table
create table kaantaja_taito_kohdealue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kohdealueOtsikko_id int8,
    kaantajaTaito_id int8,
    kohdealueet_ORDER int4,
    primary key (id, REV)
);

-- Join table for tutkintovaatimus (OneToMany with order)
create table kaantaja_taito_kohdealue_tutkintovaatimus (
    kaantaja_taito_kohdealue_id int8 not null,
    tekstipalanen_id int8 not null,
    tutkintovaatimukset_ORDER int4 not null,
    primary key (kaantaja_taito_kohdealue_id, tutkintovaatimukset_ORDER)
);

-- Audit table for tutkintovaatimus join table
create table kaantaja_taito_kohdealue_tutkintovaatimus_AUD (
    REV int4 not null,
    kaantaja_taito_kohdealue_id int8 not null,
    tekstipalanen_id int8 not null,
    tutkintovaatimukset_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, kaantaja_taito_kohdealue_id, tekstipalanen_id, tutkintovaatimukset_ORDER)
);

-- Join table for arviointikriteeri (OneToMany with order)
create table kaantaja_taito_kohdealue_arviointikriteeri (
    kaantaja_taito_kohdealue_id int8 not null,
    tekstipalanen_id int8 not null,
    arviointikriteerit_ORDER int4 not null,
    primary key (kaantaja_taito_kohdealue_id, arviointikriteerit_ORDER)
);

-- Audit table for arviointikriteeri join table
create table kaantaja_taito_kohdealue_arviointikriteeri_AUD (
    REV int4 not null,
    kaantaja_taito_kohdealue_id int8 not null,
    tekstipalanen_id int8 not null,
    arviointikriteerit_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, kaantaja_taito_kohdealue_id, tekstipalanen_id, arviointikriteerit_ORDER)
);

-- Foreign key constraints for kaantaja_taito
alter table kaantaja_taito
    add constraint FK_kaantaja_taito_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_taito
    add constraint FK_kaantaja_taito_valiotsikko
    foreign key (valiotsikko_id)
    references tekstipalanen;

alter table kaantaja_taito
    add constraint FK_kaantaja_taito_perusteenosa
    foreign key (id)
    references perusteenosa;

-- Foreign key constraints for kaantaja_taito audit
alter table kaantaja_taito_AUD
    add constraint FK_kaantaja_taito_AUD_perusteenosa_AUD
    foreign key (id, REV)
    references perusteenosa_AUD;

-- Foreign key constraints for kaantaja_taito_kohdealue
alter table kaantaja_taito_kohdealue
    add constraint FK_kaantaja_taito_kohdealue_otsikko
    foreign key (kohdealueOtsikko_id)
    references tekstipalanen;

alter table kaantaja_taito_kohdealue
    add constraint FK_kaantaja_taito_kohdealue_kaantaja_taito
    foreign key (kaantajaTaito_id)
    references kaantaja_taito;

-- Foreign key constraints for kaantaja_taito_kohdealue audit
alter table kaantaja_taito_kohdealue_AUD
    add constraint FK_kaantaja_taito_kohdealue_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_taito_kohdealue_AUD
    add constraint FK_kaantaja_taito_kohdealue_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for tutkintovaatimus join table
alter table kaantaja_taito_kohdealue_tutkintovaatimus
    add constraint FK_kaantaja_taito_kohdealue_tutkintovaatimus_kohdealue
    foreign key (kaantaja_taito_kohdealue_id)
    references kaantaja_taito_kohdealue;

alter table kaantaja_taito_kohdealue_tutkintovaatimus
    add constraint FK_kaantaja_taito_kohdealue_tutkintovaatimus_tekstipalanen
    foreign key (tekstipalanen_id)
    references tekstipalanen;

-- Foreign key constraints for tutkintovaatimus audit join table
alter table kaantaja_taito_kohdealue_tutkintovaatimus_AUD
    add constraint FK_kaantaja_taito_kohdealue_tutkintovaatimus_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_taito_kohdealue_tutkintovaatimus_AUD
    add constraint FK_kaantaja_taito_kohdealue_tutkintovaatimus_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for arviointikriteeri join table
alter table kaantaja_taito_kohdealue_arviointikriteeri
    add constraint FK_kaantaja_taito_kohdealue_arviointikriteeri_kohdealue
    foreign key (kaantaja_taito_kohdealue_id)
    references kaantaja_taito_kohdealue;

alter table kaantaja_taito_kohdealue_arviointikriteeri
    add constraint FK_kaantaja_taito_kohdealue_arviointikriteeri_tekstipalanen
    foreign key (tekstipalanen_id)
    references tekstipalanen;

-- Foreign key constraints for arviointikriteeri audit join table
alter table kaantaja_taito_kohdealue_arviointikriteeri_AUD
    add constraint FK_kaantaja_taito_kohdealue_arviointikriteeri_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_taito_kohdealue_arviointikriteeri_AUD
    add constraint FK_kaantaja_taito_kohdealue_arviointikriteeri_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

