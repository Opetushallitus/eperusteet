create table aipeoppiaine_aipekurssi (
    oppiaine_id int8 not null,
    kurssi_id int8 not null,
    kurssit_order int4 not null,
    primary key (oppiaine_id, kurssit_order)
);

create table aipeoppiaine_aipekurssi_AUD (
    REV int4 not null,
    oppiaine_id int8 not null,
    kurssi_id int8 not null,
    kurssit_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, kurssi_id, kurssit_order)
);

create table aipeoppiaine_aipeoppiaine (
    oppiaine_id int8 not null,
    oppimaara_id int8 not null,
    oppimaara_order int4 not null,
    primary key (oppiaine_id, oppimaara_order)
);

create table aipeoppiaine_aipeoppiaine_AUD (
    REV int4 not null,
    oppiaine_id int8 not null,
    oppimaara_id int8 not null,
    oppimaara_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, oppimaara_id, oppimaara_order)
);

create table public.yl_aipe_kurssi (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tunniste uuid not null,
    koodi_id int8,
    kuvaus_id int8,
    nimi_id int8,
    primary key (id)
);

create table public.yl_aipe_kurssi_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tunniste uuid,
    koodi_id int8,
    kuvaus_id int8,
    nimi_id int8,
    primary key (id, REV)
);

create table yl_aipe_opetuksensisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8 not null,
    sisalto_id int8,
    primary key (id)
);

create table yl_aipe_opetuksensisalto_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8,
    sisalto_id int8,
    primary key (id, REV)
);

create table yl_aipe_opetuksensisalto_yl_aipe_vaihe (
    yl_aipe_opetuksensisalto_id int8 not null,
    vaiheet_id int8 not null,
    primary key (yl_aipe_opetuksensisalto_id, vaiheet_id)
);

create table yl_aipe_opetuksensisalto_yl_aipe_vaihe_AUD (
    REV int4 not null,
    yl_aipe_opetuksensisalto_id int8 not null,
    vaiheet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, yl_aipe_opetuksensisalto_id, vaiheet_id)
);

create table yl_aipe_oppiaine (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    abstrakti boolean not null,
    koosteinen boolean not null,
    tunniste uuid not null,
    arviointi_id int8,
    koodi_id int8,
    nimi_id int8,
    pakollinenKurssiKuvaus_id int8,
    soveltava_kurssi_kuvaus int8,
    syventava_kurssi_kuvaus int8,
    tavoitteet_id int8,
    tehtava_id int8,
    primary key (id)
);

create table yl_aipe_oppiaine_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    abstrakti boolean,
    koosteinen boolean,
    tunniste uuid,
    arviointi_id int8,
    koodi_id int8,
    nimi_id int8,
    pakollinenKurssiKuvaus_id int8,
    soveltava_kurssi_kuvaus int8,
    syventava_kurssi_kuvaus int8,
    tavoitteet_id int8,
    tehtava_id int8,
    primary key (id, REV)
);

create table yl_aipe_vaihe (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    primary key (id)
);

create table yl_aipe_vaihe_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (id, REV)
);

alter table aipeoppiaine_aipekurssi
    add constraint UK_76yth2um3xyrlnx01jrmyexv5  unique (kurssi_id);

alter table aipeoppiaine_aipeoppiaine
    add constraint UK_1np73635586vpqmknf5cust0q  unique (oppimaara_id);

alter table public.yl_aipe_kurssi
    add constraint UK_fmby2ha9jh1xhynavvpi08mpj  unique (tunniste);

alter table yl_aipe_opetuksensisalto
    add constraint UK_4nqcxldr6s0h4ri5reoeb7u91  unique (peruste_id);

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe
    add constraint UK_2b125utu3e10xhojc3c764pyj  unique (vaiheet_id);

alter table aipeoppiaine_aipekurssi
    add constraint FK_76yth2um3xyrlnx01jrmyexv5
    foreign key (kurssi_id)
    references public.yl_aipe_kurssi;

alter table aipeoppiaine_aipekurssi
    add constraint FK_shy01shno4dvjom2n5605bicp
    foreign key (oppiaine_id)
    references yl_aipe_oppiaine;

alter table aipeoppiaine_aipekurssi_AUD
    add constraint FK_liacyfrp88o3ujjdyi3qbva7n
    foreign key (REV)
    references revinfo;

alter table aipeoppiaine_aipekurssi_AUD
    add constraint FK_6phdbndm3hi6uxf9knn9ax4tv
    foreign key (REVEND)
    references revinfo;

alter table aipeoppiaine_aipeoppiaine
    add constraint FK_1np73635586vpqmknf5cust0q
    foreign key (oppimaara_id)
    references yl_aipe_oppiaine;

alter table aipeoppiaine_aipeoppiaine
    add constraint FK_s6f0989y6d53xsvttxvh0eqf2
    foreign key (oppiaine_id)
    references yl_aipe_oppiaine;

alter table aipeoppiaine_aipeoppiaine_AUD
    add constraint FK_svwcknog5x0qmy9gqfu1u80tc
    foreign key (REV)
    references revinfo;

alter table aipeoppiaine_aipeoppiaine_AUD
    add constraint FK_17yjjh4th0nka7t1923fqc9o4
    foreign key (REVEND)
    references revinfo;

alter table public.yl_aipe_kurssi
    add constraint FK_iqbhw8g280j59xukoc0f7wf5q
    foreign key (koodi_id)
    references koodi;

alter table public.yl_aipe_kurssi
    add constraint FK_ixsdfamkdr1gwwgu2mmitqf3u
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table public.yl_aipe_kurssi
    add constraint FK_1501j8s2p3hue61hb2u7oi0s3
    foreign key (nimi_id)
    references tekstipalanen;

alter table public.yl_aipe_kurssi_AUD
    add constraint FK_ce9gucttri6fk6dbc6ftp3kyg
    foreign key (REV)
    references revinfo;

alter table public.yl_aipe_kurssi_AUD
    add constraint FK_gnbw1q3p4wvemxixn5clqi2hw
    foreign key (REVEND)
    references revinfo;

alter table yl_aipe_opetuksensisalto
    add constraint FK_4nqcxldr6s0h4ri5reoeb7u91
    foreign key (peruste_id)
    references peruste;

alter table yl_aipe_opetuksensisalto
    add constraint FK_1thio2jlde4hwbixq4e3q6akt
    foreign key (sisalto_id)
    references perusteenosaviite;

alter table yl_aipe_opetuksensisalto_AUD
    add constraint FK_ag14gtw71fd0o21g90gics7pp
    foreign key (REV)
    references revinfo;

alter table yl_aipe_opetuksensisalto_AUD
    add constraint FK_1ygpt4m4el35pho92k7nxu0hp
    foreign key (REVEND)
    references revinfo;

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe
    add constraint FK_2b125utu3e10xhojc3c764pyj
    foreign key (vaiheet_id)
    references yl_aipe_vaihe;

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe
    add constraint FK_fq70oywteyoxoks7k3sfsjs8f
    foreign key (yl_aipe_opetuksensisalto_id)
    references yl_aipe_opetuksensisalto;

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe_AUD
    add constraint FK_9rmxxlb0grd9u146ovbiq0s32
    foreign key (REV)
    references revinfo;

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe_AUD
    add constraint FK_hq5rs4b45kg9ppwoi9ppovrgc
    foreign key (REVEND)
    references revinfo;

alter table yl_aipe_oppiaine
    add constraint FK_lipd29iah43cs42mh6je14jyb
    foreign key (arviointi_id)
    references yl_tekstiosa;

alter table yl_aipe_oppiaine
    add constraint FK_etnasxqlnyaf3roh4ku496chs
    foreign key (koodi_id)
    references koodi;

alter table yl_aipe_oppiaine
    add constraint FK_364gf55yk7xasficn9iwqy6qd
    foreign key (nimi_id)
    references tekstipalanen;

alter table yl_aipe_oppiaine
    add constraint FK_7d4bkp06j5c7uhhlvn7ptcbng
    foreign key (pakollinenKurssiKuvaus_id)
    references tekstipalanen;

alter table yl_aipe_oppiaine
    add constraint FK_2k2mhx3bp31gu28740xcobxhs
    foreign key (soveltava_kurssi_kuvaus)
    references tekstipalanen;

alter table yl_aipe_oppiaine
    add constraint FK_78a5otc7obbip2hkjnwtut3n2
    foreign key (syventava_kurssi_kuvaus)
    references tekstipalanen;

alter table yl_aipe_oppiaine
    add constraint FK_qrnbtu69ywsjyur6gj2vmwt8d
    foreign key (tavoitteet_id)
    references yl_tekstiosa;

alter table yl_aipe_oppiaine
    add constraint FK_pa711ku4dcdattotbvdjtues0
    foreign key (tehtava_id)
    references yl_tekstiosa;

alter table yl_aipe_oppiaine_AUD
    add constraint FK_ataxn38vn07dpes9nyomvdxuy
    foreign key (REV)
    references revinfo;

alter table yl_aipe_oppiaine_AUD
    add constraint FK_c24ccjukc9bcs89s1o5vn8n0
    foreign key (REVEND)
    references revinfo;

alter table yl_aipe_vaihe_AUD
    add constraint FK_rajwj5tu06oc8pb0chvnvrahs
    foreign key (REV)
    references revinfo;

alter table yl_aipe_vaihe_AUD
    add constraint FK_qxe806d1krhtmxgs4q9e26ine
    foreign key (REVEND)
    references revinfo;
