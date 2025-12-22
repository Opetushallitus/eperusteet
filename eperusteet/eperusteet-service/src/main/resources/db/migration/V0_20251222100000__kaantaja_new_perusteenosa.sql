-- KaantajaKielitaito main table (extends perusteenosa)
create table kaantaja_kielitaito (
    id int8 not null,
    kuvaus_id int8,
    liite boolean not null default false,
    primary key (id)
);

-- KaantajaKielitaito audit table
create table kaantaja_kielitaito_AUD (
    id int8 not null,
    REV int4 not null,
    kuvaus_id int8,
    liite boolean,
    primary key (id, REV)
);

-- KaantajaKielitaitoTaitotaso main table
create table kaantaja_kielitaito_taitotaso (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kuvaus_id int8,
    taitotaso_id int8,
    kaantajaKielitaito_id int8,
    taitotasot_ORDER int4,
    primary key (id)
);

-- KaantajaKielitaitoTaitotaso audit table
create table kaantaja_kielitaito_taitotaso_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kuvaus_id int8,
    taitotaso_id int8,
    kaantajaKielitaito_id int8,
    taitotasot_ORDER int4,
    primary key (id, REV)
);

-- KaantajaTaitotasokuvaus main table (extends perusteenosa)
create table kaantaja_taitotasokuvaus (
    id int8 not null,
    kuvaus_id int8,
    liite boolean not null default false,
    primary key (id)
);

-- KaantajaTaitotasokuvaus audit table
create table kaantaja_taitotasokuvaus_AUD (
    id int8 not null,
    REV int4 not null,
    kuvaus_id int8,
    liite boolean,
    primary key (id, REV)
);

-- KaantajaTaitotasoTutkintotaso main table
create table kaantaja_taitotaso_tutkintotaso (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    kaantajaTaitotasokuvaus_id int8,
    tutkintotasot_ORDER int4,
    primary key (id)
);

-- KaantajaTaitotasoTutkintotaso audit table
create table kaantaja_taitotaso_tutkintotaso_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    kaantajaTaitotasokuvaus_id int8,
    tutkintotasot_ORDER int4,
    primary key (id, REV)
);

-- KaantajaTaitotasoTutkintotasoOsa main table
create table kaantaja_taitotaso_tutkintotaso_osa (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    suorituksenOsa_id int8,
    kaantajaTaitotasoTutkintotaso_id int8,
    osat_ORDER int4,
    primary key (id)
);

-- KaantajaTaitotasoTutkintotasoOsa audit table
create table kaantaja_taitotaso_tutkintotaso_osa_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    suorituksenOsa_id int8,
    kaantajaTaitotasoTutkintotaso_id int8,
    osat_ORDER int4,
    primary key (id, REV)
);

-- KaantajaTaitotasoTutkintotasoOsaTaitotaso main table
create table kaantaja_taitotaso_tutkintotaso_osa_taitotaso (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    taitotaso_id int8,
    kuvaus_id int8,
    kaantajaTaitotasoTutkintotasoOsa_id int8,
    taitotasot_ORDER int4,
    primary key (id)
);

-- KaantajaTaitotasoTutkintotasoOsaTaitotaso audit table
create table kaantaja_taitotaso_tutkintotaso_osa_taitotaso_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    taitotaso_id int8,
    kuvaus_id int8,
    kaantajaTaitotasoTutkintotasoOsa_id int8,
    taitotasot_ORDER int4,
    primary key (id, REV)
);

-- KaantajaAihealue main table (extends perusteenosa)
create table kaantaja_aihealue (
    id int8 not null,
    kuvaus_id int8,
    liite boolean not null default false,
    primary key (id)
);

-- KaantajaAihealue audit table
create table kaantaja_aihealue_AUD (
    id int8 not null,
    REV int4 not null,
    kuvaus_id int8,
    liite boolean,
    primary key (id, REV)
);

-- KaantajaAihealueKategoria main table
create table kaantaja_aihealue_kategoria (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    kuvaus_id int8,
    perustaso_id int8,
    keskitaso_id int8,
    ylintaso_id int8,
    kaantajaAihealue_id int8,
    kategoriat_ORDER int4,
    primary key (id)
);

-- KaantajaAihealueKategoria audit table
create table kaantaja_aihealue_kategoria_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    kuvaus_id int8,
    perustaso_id int8,
    keskitaso_id int8,
    ylintaso_id int8,
    kaantajaAihealue_id int8,
    kategoriat_ORDER int4,
    primary key (id, REV)
);

-- KaantajaTodistusmalli main table (extends perusteenosa)
create table kaantaja_todistusmalli (
    id int8 not null,
    kuvaus_id int8,
    ylintaso_id int8,
    keskitaso_id int8,
    perustaso_id int8,
    liite boolean not null default false,
    primary key (id)
);

-- KaantajaTodistusmalli audit table
create table kaantaja_todistusmalli_AUD (
    id int8 not null,
    REV int4 not null,
    kuvaus_id int8,
    ylintaso_id int8,
    keskitaso_id int8,
    perustaso_id int8,
    liite boolean,
    primary key (id, REV)
);

-- KaantajaTodistusmalliTaitotasokuvaus main table
create table kaantaja_todistusmalli_taitotasokuvaus (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    primary key (id)
);

-- KaantajaTodistusmalliTaitotasokuvaus audit table
create table kaantaja_todistusmalli_taitotasokuvaus_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    primary key (id, REV)
);

-- KaantajaTodistusmalliTaitotaso main table
create table kaantaja_todistusmalli_taitotaso (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    taitotaso_id int8,
    asteikko_id int8,
    kuvaus_id int8,
    kaantajaTodistusmalliTaitotasokuvaus_id int8,
    taitotasot_ORDER int4,
    primary key (id)
);

-- KaantajaTodistusmalliTaitotaso audit table
create table kaantaja_todistusmalli_taitotaso_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    taitotaso_id int8,
    asteikko_id int8,
    kuvaus_id int8,
    kaantajaTodistusmalliTaitotasokuvaus_id int8,
    taitotasot_ORDER int4,
    primary key (id, REV)
);

-- Foreign key constraints for kaantaja_kielitaito
alter table kaantaja_kielitaito
    add constraint FK_kaantaja_kielitaito_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_kielitaito
    add constraint FK_kaantaja_kielitaito_perusteenosa
    foreign key (id)
    references perusteenosa;

-- Foreign key constraints for kaantaja_kielitaito audit
alter table kaantaja_kielitaito_AUD
    add constraint FK_kaantaja_kielitaito_AUD_perusteenosa_AUD
    foreign key (id, REV)
    references perusteenosa_AUD;

-- Foreign key constraints for kaantaja_kielitaito_taitotaso
alter table kaantaja_kielitaito_taitotaso
    add constraint FK_kaantaja_kielitaito_taitotaso_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_kielitaito_taitotaso
    add constraint FK_kaantaja_kielitaito_taitotaso_taitotaso
    foreign key (taitotaso_id)
    references koodi;

alter table kaantaja_kielitaito_taitotaso
    add constraint FK_kaantaja_kielitaito_taitotaso_kaantaja_kielitaito
    foreign key (kaantajaKielitaito_id)
    references kaantaja_kielitaito;

-- Foreign key constraints for kaantaja_kielitaito_taitotaso audit
alter table kaantaja_kielitaito_taitotaso_AUD
    add constraint FK_kaantaja_kielitaito_taitotaso_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_kielitaito_taitotaso_AUD
    add constraint FK_kaantaja_kielitaito_taitotaso_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for kaantaja_taitotasokuvaus
alter table kaantaja_taitotasokuvaus
    add constraint FK_kaantaja_taitotasokuvaus_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_taitotasokuvaus
    add constraint FK_kaantaja_taitotasokuvaus_perusteenosa
    foreign key (id)
    references perusteenosa;

-- Foreign key constraints for kaantaja_taitotasokuvaus audit
alter table kaantaja_taitotasokuvaus_AUD
    add constraint FK_kaantaja_taitotasokuvaus_AUD_perusteenosa_AUD
    foreign key (id, REV)
    references perusteenosa_AUD;

-- Foreign key constraints for kaantaja_taitotaso_tutkintotaso
alter table kaantaja_taitotaso_tutkintotaso
    add constraint FK_kaantaja_taitotaso_tutkintotaso_nimi
    foreign key (nimi_id)
    references tekstipalanen;

alter table kaantaja_taitotaso_tutkintotaso
    add constraint FK_kaantaja_taitotaso_tutkintotaso_kaantaja_taitotasokuvaus
    foreign key (kaantajaTaitotasokuvaus_id)
    references kaantaja_taitotasokuvaus;

-- Foreign key constraints for kaantaja_taitotaso_tutkintotaso audit
alter table kaantaja_taitotaso_tutkintotaso_AUD
    add constraint FK_kaantaja_taitotaso_tutkintotaso_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_taitotaso_tutkintotaso_AUD
    add constraint FK_kaantaja_taitotaso_tutkintotaso_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for kaantaja_taitotaso_tutkintotaso_osa
alter table kaantaja_taitotaso_tutkintotaso_osa
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_suorituksenOsa
    foreign key (suorituksenOsa_id)
    references koodi;

alter table kaantaja_taitotaso_tutkintotaso_osa
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_tutkintotaso
    foreign key (kaantajaTaitotasoTutkintotaso_id)
    references kaantaja_taitotaso_tutkintotaso;

-- Foreign key constraints for kaantaja_taitotaso_tutkintotaso_osa audit
alter table kaantaja_taitotaso_tutkintotaso_osa_AUD
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_taitotaso_tutkintotaso_osa_AUD
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for kaantaja_taitotaso_tutkintotaso_osa_taitotaso
alter table kaantaja_taitotaso_tutkintotaso_osa_taitotaso
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_taitotaso_taitotaso
    foreign key (taitotaso_id)
    references koodi;

alter table kaantaja_taitotaso_tutkintotaso_osa_taitotaso
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_taitotaso_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_taitotaso_tutkintotaso_osa_taitotaso
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_taitotaso_osa
    foreign key (kaantajaTaitotasoTutkintotasoOsa_id)
    references kaantaja_taitotaso_tutkintotaso_osa;

-- Foreign key constraints for kaantaja_taitotaso_tutkintotaso_osa_taitotaso audit
alter table kaantaja_taitotaso_tutkintotaso_osa_taitotaso_AUD
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_taitotaso_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_taitotaso_tutkintotaso_osa_taitotaso_AUD
    add constraint FK_kaantaja_taitotaso_tutkintotaso_osa_taitotaso_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for kaantaja_aihealue
alter table kaantaja_aihealue
    add constraint FK_kaantaja_aihealue_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_aihealue
    add constraint FK_kaantaja_aihealue_perusteenosa
    foreign key (id)
    references perusteenosa;

-- Foreign key constraints for kaantaja_aihealue audit
alter table kaantaja_aihealue_AUD
    add constraint FK_kaantaja_aihealue_AUD_perusteenosa_AUD
    foreign key (id, REV)
    references perusteenosa_AUD;

-- Foreign key constraints for kaantaja_aihealue_kategoria
alter table kaantaja_aihealue_kategoria
    add constraint FK_kaantaja_aihealue_kategoria_nimi
    foreign key (nimi_id)
    references tekstipalanen;

alter table kaantaja_aihealue_kategoria
    add constraint FK_kaantaja_aihealue_kategoria_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_aihealue_kategoria
    add constraint FK_kaantaja_aihealue_kategoria_perustaso
    foreign key (perustaso_id)
    references tekstipalanen;

alter table kaantaja_aihealue_kategoria
    add constraint FK_kaantaja_aihealue_kategoria_keskitaso
    foreign key (keskitaso_id)
    references tekstipalanen;

alter table kaantaja_aihealue_kategoria
    add constraint FK_kaantaja_aihealue_kategoria_ylintaso
    foreign key (ylintaso_id)
    references tekstipalanen;

alter table kaantaja_aihealue_kategoria
    add constraint FK_kaantaja_aihealue_kategoria_kaantaja_aihealue
    foreign key (kaantajaAihealue_id)
    references kaantaja_aihealue;

-- Foreign key constraints for kaantaja_aihealue_kategoria audit
alter table kaantaja_aihealue_kategoria_AUD
    add constraint FK_kaantaja_aihealue_kategoria_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_aihealue_kategoria_AUD
    add constraint FK_kaantaja_aihealue_kategoria_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for kaantaja_todistusmalli
alter table kaantaja_todistusmalli
    add constraint FK_kaantaja_todistusmalli_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_todistusmalli
    add constraint FK_kaantaja_todistusmalli_ylintaso
    foreign key (ylintaso_id)
    references kaantaja_todistusmalli_taitotasokuvaus;

alter table kaantaja_todistusmalli
    add constraint FK_kaantaja_todistusmalli_keskitaso
    foreign key (keskitaso_id)
    references kaantaja_todistusmalli_taitotasokuvaus;

alter table kaantaja_todistusmalli
    add constraint FK_kaantaja_todistusmalli_perustaso
    foreign key (perustaso_id)
    references kaantaja_todistusmalli_taitotasokuvaus;

alter table kaantaja_todistusmalli
    add constraint FK_kaantaja_todistusmalli_perusteenosa
    foreign key (id)
    references perusteenosa;

-- Foreign key constraints for kaantaja_todistusmalli audit
alter table kaantaja_todistusmalli_AUD
    add constraint FK_kaantaja_todistusmalli_AUD_perusteenosa_AUD
    foreign key (id, REV)
    references perusteenosa_AUD;

-- Foreign key constraints for kaantaja_todistusmalli_taitotasokuvaus audit
alter table kaantaja_todistusmalli_taitotasokuvaus_AUD
    add constraint FK_kaantaja_todistusmalli_taitotasokuvaus_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_todistusmalli_taitotasokuvaus_AUD
    add constraint FK_kaantaja_todistusmalli_taitotasokuvaus_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

-- Foreign key constraints for kaantaja_todistusmalli_taitotaso
alter table kaantaja_todistusmalli_taitotaso
    add constraint FK_kaantaja_todistusmalli_taitotaso_taitotaso
    foreign key (taitotaso_id)
    references koodi;

alter table kaantaja_todistusmalli_taitotaso
    add constraint FK_kaantaja_todistusmalli_taitotaso_asteikko
    foreign key (asteikko_id)
    references tekstipalanen;

alter table kaantaja_todistusmalli_taitotaso
    add constraint FK_kaantaja_todistusmalli_taitotaso_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table kaantaja_todistusmalli_taitotaso
    add constraint FK_kaantaja_todistusmalli_taitotaso_taitotasokuvaus
    foreign key (kaantajaTodistusmalliTaitotasokuvaus_id)
    references kaantaja_todistusmalli_taitotasokuvaus;

-- Foreign key constraints for kaantaja_todistusmalli_taitotaso audit
alter table kaantaja_todistusmalli_taitotaso_AUD
    add constraint FK_kaantaja_todistusmalli_taitotaso_AUD_REV
    foreign key (REV)
    references revinfo;

alter table kaantaja_todistusmalli_taitotaso_AUD
    add constraint FK_kaantaja_todistusmalli_taitotaso_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

