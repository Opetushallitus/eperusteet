alter table peruste add column toteutus varchar(255);
alter table peruste_AUD add column toteutus varchar(255);

create table yl_lops2019_laaja_alainen_osaaminen (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    kuvaus_id int8,
    nimi_id int8,
    opinnot_id int8,
    primary key (id)
);

create table yl_lops2019_laaja_alainen_osaaminen_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    kuvaus_id int8,
    nimi_id int8,
    opinnot_id int8,
    primary key (id, REV)
);

create table yl_lops2019_laaja_alainen_osaaminen_kokonaisuus (
    id int8 not null,
    primary key (id)
);

create table yl_lops2019_laaja_alainen_osaaminen_kokonaisuus_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (id, REV)
);

create table yl_lops2019_laaja_alainen_osaaminen_painopiste (
    laaja_alainen_osaaminen_id int8 not null,
    painopiste_id int8 not null
);

create table yl_lops2019_laaja_alainen_osaaminen_painopiste_AUD (
    REV int4 not null,
    laaja_alainen_osaaminen_id int8 not null,
    painopiste_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, laaja_alainen_osaaminen_id, painopiste_id)
);

create table yl_lops2019_laaja_alainen_osaaminen_tavoite (
    laaja_alainen_osaaminen_id int8 not null,
    tavoite_id int8 not null
);

create table yl_lops2019_laaja_alainen_osaaminen_tavoite_AUD (
    REV int4 not null,
    laaja_alainen_osaaminen_id int8 not null,
    tavoite_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, laaja_alainen_osaaminen_id, tavoite_id)
);

create table yl_lops2019_lao_kokonaisuus_lao (
    laaja_alainen_osaaminen_kokonaisuus_id int8 not null,
    laaja_alainen_osaaminen_id int8 not null
);

create table yl_lops2019_lao_kokonaisuus_lao_AUD (
    REV int4 not null,
    laaja_alainen_osaaminen_kokonaisuus_id int8 not null,
    laaja_alainen_osaaminen_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, laaja_alainen_osaaminen_kokonaisuus_id, laaja_alainen_osaaminen_id)
);

create table yl_lops2019_moduuli (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    laajuus numeric(10, 2),
    pakollinen boolean not null,
    koodi_id int8,
    kuvaus_id int8,
    nimi_id int8 not null,
    tavoitteet_id int8,
    primary key (id)
);

create table yl_lops2019_moduuli_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    laajuus numeric(10, 2),
    pakollinen boolean,
    koodi_id int8,
    kuvaus_id int8,
    nimi_id int8,
    tavoitteet_id int8,
    primary key (id, REV)
);

create table yl_lops2019_moduuli_sisalto (
    id int8 not null,
    kohde_id int8,
    primary key (id)
);

create table yl_lops2019_moduuli_sisalto_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kohde_id int8,
    primary key (id, REV)
);

create table yl_lops2019_oppiaine (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    arviointi_id int8,
    koodi_id int8,
    nimi_id int8 not null,
    tavoitteet_id int8,
    tehtava_id int8,
    primary key (id)
);

create table yl_lops2019_oppiaine_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    arviointi_id int8,
    koodi_id int8,
    nimi_id int8,
    tavoitteet_id int8,
    tehtava_id int8,
    primary key (id, REV)
);

create table yl_lops2019_oppiaine_arviointi (
    id int8 not null,
    kuvaus_id int8,
    primary key (id)
);

create table yl_lops2019_oppiaine_arviointi_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kuvaus_id int8,
    primary key (id, REV)
);

create table yl_lops2019_oppiaine_laaja_alainen_osaaminen (
    id int8 not null,
    jarjestys int4,
    kuvaus_id int8,
    primary key (id)
);

create table yl_lops2019_oppiaine_laaja_alainen_osaaminen_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    jarjestys int4,
    kuvaus_id int8,
    primary key (id, REV)
);

create table yl_lops2019_oppiaine_laaja_alainen_osaaminen_koodi (
    laaja_alainen_osaaminen_id int8 not null,
    koodi_id int8 not null,
    primary key (laaja_alainen_osaaminen_id, koodi_id)
);

create table yl_lops2019_oppiaine_laaja_alainen_osaaminen_koodi_AUD (
    REV int4 not null,
    laaja_alainen_osaaminen_id int8 not null,
    koodi_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, laaja_alainen_osaaminen_id, koodi_id)
);

create table yl_lops2019_oppiaine_laaja_alaiset_osaamiset (
    oppiaine_id int8 not null,
    laaja_alainen_osaaminen_id int8 not null,
    laajaAlaisetOsaamiset_ORDER int4 not null,
    primary key (oppiaine_id, laajaAlaisetOsaamiset_ORDER)
);

create table yl_lops2019_oppiaine_laaja_alaiset_osaamiset_AUD (
    REV int4 not null,
    oppiaine_id int8 not null,
    laaja_alainen_osaaminen_id int8 not null,
    laajaAlaisetOsaamiset_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, laaja_alainen_osaaminen_id, laajaAlaisetOsaamiset_ORDER)
);

create table yl_lops2019_oppiaine_moduuli (
    oppiaine_id int8 not null,
    moduuli_id int8 not null
);

create table yl_lops2019_oppiaine_moduuli_AUD (
    REV int4 not null,
    oppiaine_id int8 not null,
    moduuli_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, moduuli_id)
);

create table yl_lops2019_oppiaine_moduuli_sisalto (
    moduuli_id int8 not null,
    sisalto_id int8 not null
);

create table yl_lops2019_oppiaine_moduuli_sisalto_AUD (
    REV int4 not null,
    moduuli_id int8 not null,
    sisalto_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, moduuli_id, sisalto_id)
);

create table yl_lops2019_oppiaine_moduuli_sisalto_tekstipalanen (
    sisalto_id int8 not null,
    tekstipalanen_id int8 not null,
    sisallot_ORDER int4 not null,
    primary key (sisalto_id, sisallot_ORDER)
);

create table yl_lops2019_oppiaine_moduuli_sisalto_tekstipalanen_AUD (
    REV int4 not null,
    sisalto_id int8 not null,
    tekstipalanen_id int8 not null,
    sisallot_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, sisalto_id, tekstipalanen_id, sisallot_ORDER)
);

create table yl_lops2019_oppiaine_moduuli_tavoite (
    id int8 not null,
    kohde_id int8,
    primary key (id)
);

create table yl_lops2019_oppiaine_moduuli_tavoite_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kohde_id int8,
    primary key (id, REV)
);

create table yl_lops2019_oppiaine_moduuli_tavoite_tekstipalanen (
    tavoite_id int8 not null,
    tekstipalanen_id int8 not null,
    tavoitteet_ORDER int4 not null,
    primary key (tavoite_id, tavoitteet_ORDER)
);

create table yl_lops2019_oppiaine_moduuli_tavoite_tekstipalanen_AUD (
    REV int4 not null,
    tavoite_id int8 not null,
    tekstipalanen_id int8 not null,
    tavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tavoite_id, tekstipalanen_id, tavoitteet_ORDER)
);

create table yl_lops2019_oppiaine_oppimaara (
    oppiaine_id int8 not null,
    oppimaara_id int8 not null
);

create table yl_lops2019_oppiaine_oppimaara_AUD (
    REV int4 not null,
    oppiaine_id int8 not null,
    oppimaara_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, oppimaara_id)
);

create table yl_lops2019_oppiaine_painopiste (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    nimi_id int8,
    primary key (id)
);

create table yl_lops2019_oppiaine_painopiste_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    nimi_id int8,
    primary key (id, REV)
);

create table yl_lops2019_oppiaine_tavoitealue (
    id int8 not null,
    kohde_id int8,
    nimi_id int8 not null,
    primary key (id)
);

create table yl_lops2019_oppiaine_tavoitealue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kohde_id int8,
    nimi_id int8,
    primary key (id, REV)
);

create table yl_lops2019_oppiaine_tavoitealue_tekstipalanen (
    tavoitealue_id int8 not null,
    tekstipalanen_id int8 not null,
    tavoitteet_ORDER int4 not null,
    primary key (tavoitealue_id, tavoitteet_ORDER)
);

create table yl_lops2019_oppiaine_tavoitealue_tekstipalanen_AUD (
    REV int4 not null,
    tavoitealue_id int8 not null,
    tekstipalanen_id int8 not null,
    tavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tavoitealue_id, tekstipalanen_id, tavoitteet_ORDER)
);

create table yl_lops2019_oppiaine_tavoitteet (
    id int8 not null,
    kuvaus_id int8,
    primary key (id)
);

create table yl_lops2019_oppiaine_tavoitteet_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kuvaus_id int8,
    primary key (id, REV)
);

create table yl_lops2019_oppiaine_tavoitteet_tavoitealue (
    tavoitteet_id int8 not null,
    tavoitealue_id int8 not null,
    tavoitealueet_ORDER int4 not null,
    primary key (tavoitteet_id, tavoitealueet_ORDER)
);

create table yl_lops2019_oppiaine_tavoitteet_tavoitealue_AUD (
    REV int4 not null,
    tavoitteet_id int8 not null,
    tavoitealue_id int8 not null,
    tavoitealueet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tavoitteet_id, tavoitealue_id, tavoitealueet_ORDER)
);

create table yl_lops2019_oppiaine_tehtava (
    id int8 not null,
    kuvaus_id int8,
    primary key (id)
);

create table yl_lops2019_oppiaine_tehtava_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kuvaus_id int8,
    primary key (id, REV)
);

create table yl_lops2019_sisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    laaja_alainen_osaaminen_kokonaisuus_id int8 not null,
    peruste_id int8 not null,
    sisalto_id int8,
    primary key (id)
);

create table yl_lops2019_sisalto_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    laaja_alainen_osaaminen_kokonaisuus_id int8,
    peruste_id int8,
    sisalto_id int8,
    primary key (id, REV)
);

create table yl_lops2019_sisalto_oppiaine (
    sisalto_id int8 not null,
    oppiaine_id int8 not null
);

create table yl_lops2019_sisalto_oppiaine_AUD (
    REV int4 not null,
    sisalto_id int8 not null,
    oppiaine_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, sisalto_id, oppiaine_id)
);

create table yl_lops2019_tavoite (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    kohde_id int8,
    primary key (id)
);

create table yl_lops2019_tavoite_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    jarjestys int4,
    kohde_id int8,
    primary key (id, REV)
);

create table yl_lops2019_tavoite_tavoite (
    id int8 not null,
    jarjestys int4,
    kuvaus_id int8,
    primary key (id)
);

create table yl_lops2019_tavoite_tavoite_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    jarjestys int4,
    kuvaus_id int8,
    primary key (id, REV)
);

create table yl_lops2019_tavoite_tavoite_tavoite (
    tavoite_id int8 not null,
    tavoite_tavoite_id int8 not null
);

create table yl_lops2019_tavoite_tavoite_tavoite_AUD (
    REV int4 not null,
    tavoite_id int8 not null,
    tavoite_tavoite_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tavoite_id, tavoite_tavoite_id)
);