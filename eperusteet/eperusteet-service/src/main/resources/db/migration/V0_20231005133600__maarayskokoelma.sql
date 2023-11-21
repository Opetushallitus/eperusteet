ALTER TABLE maarays RENAME TO muu_maarays;
ALTER TABLE maarays_url RENAME TO muu_maarays_url;

create table maarays (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    diaarinumero varchar(255),
    liittyyTyyppi varchar(255) not null,
    maarayspvm timestamp,
    tila varchar(255) not null,
    tyyppi varchar(255) not null,
    voimassaolo_alkaa timestamp,
    voimassaolo_loppuu timestamp,
    kuvaus_id int8,
    nimi_id int8 not null,
    peruste_id int8,
    primary key (id)
);

create table maarays_asiasana (
    id int8 not null,
    primary key (id)
);

create table maarays_asiasana_asiasana (
    MaaraysAsiasana_id int8 not null,
    asiasana varchar(255)
);

create table maarays_asiasanat (
    maarays_id int8 not null,
    asiasanat_id int8 not null,
    asiasanat_KEY varchar(255),
    primary key (maarays_id, asiasanat_KEY)
);

create table maarays_kieli_liite (
    maarays_kieli_liitteet_id int8 not null,
    liitteet_id uuid not null
);

create table maarays_kieli_liitteet (
    id int8 not null,
    primary key (id)
);

create table maarays_korvattavat (
    maarays_id int8 not null,
    korvattavatMaaraykset_id int8 not null
);

create table maarays_koulutustyypit (
    Maarays_id int8 not null,
    koulutustyypit varchar(255)
);

create table maarays_liite (
    id uuid not null,
    data oid not null,
    tiedostonimi varchar(1024),
    tyyppi varchar(255) not null,
    nimi_id int8,
    primary key (id)
);

create table maarays_liitteet (
    maarays_id int8 not null,
    liitteet_id int8 not null,
    liitteet_KEY varchar(255),
    primary key (maarays_id, liitteet_KEY)
);

create table maarays_muutettavat (
    maarays_id int8 not null,
    muutettavatMaaraykset_id int8 not null
);

alter table maarays_asiasanat
    add constraint UK_gi3krhen1y0kbk3bnqol0chqs  unique (asiasanat_id);

alter table maarays_kieli_liite
    add constraint UK_mg02ey8ox36a44s7htnhvw7yx  unique (liitteet_id);

alter table maarays_liitteet
    add constraint UK_qikwfeo35f9k3niqm98vaoa7o  unique (liitteet_id);

alter table maarays
    add constraint FK_okeu279b9lqashj0wrqasgr77
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table maarays
    add constraint FK_407ctd46a3ggsk5ravk23e0el
    foreign key (nimi_id)
    references tekstipalanen;

alter table maarays
    add constraint FK_l8cv2hhtipj2omvi4mvdbs1vw
    foreign key (peruste_id)
    references peruste;

alter table maarays_asiasana_asiasana
    add constraint FK_bg85dr13an1a7twmick3thxp1
    foreign key (MaaraysAsiasana_id)
    references maarays_asiasana;

alter table maarays_asiasanat
    add constraint FK_gi3krhen1y0kbk3bnqol0chqs
    foreign key (asiasanat_id)
    references maarays_asiasana;

alter table maarays_asiasanat
    add constraint FK_1f4xyqmeeh5h69jrw8gi1u9re
    foreign key (maarays_id)
    references maarays;

alter table maarays_kieli_liite
    add constraint FK_mg02ey8ox36a44s7htnhvw7yx
    foreign key (liitteet_id)
    references maarays_liite;

alter table maarays_kieli_liite
    add constraint FK_hy3stf14jd682xr5hbfvei9bh
    foreign key (maarays_kieli_liitteet_id)
    references maarays_kieli_liitteet;

alter table maarays_korvattavat
    add constraint FK_88c5bnechqfx3ip39kh0m4p3
    foreign key (korvattavatMaaraykset_id)
    references maarays;

alter table maarays_korvattavat
    add constraint FK_690m3ewh32vsh95qngd0ye160
    foreign key (maarays_id)
    references maarays;

alter table maarays_koulutustyypit
    add constraint FK_8ic4i4f1jwcsh0uxmxl002an3
    foreign key (Maarays_id)
    references maarays;

alter table maarays_liite
    add constraint FK_bjk84sbphad3p2834ifj3y73x
    foreign key (nimi_id)
    references tekstipalanen;

alter table maarays_liitteet
    add constraint FK_qikwfeo35f9k3niqm98vaoa7o
    foreign key (liitteet_id)
    references maarays_kieli_liitteet;

alter table maarays_liitteet
    add constraint FK_gevsnbn86c4xvn6t46lsoxssv
    foreign key (maarays_id)
    references maarays;

alter table maarays_muutettavat
    add constraint FK_d1iuljn36tj46lvo2ysagf4nh
    foreign key (muutettavatMaaraykset_id)
    references maarays;

alter table maarays_muutettavat
    add constraint FK_7845i2rn0titx3mmj34hdyfyj
    foreign key (maarays_id)
    references maarays;