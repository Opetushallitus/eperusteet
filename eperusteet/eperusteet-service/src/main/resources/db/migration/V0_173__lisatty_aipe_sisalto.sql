create table aipe_opetuksensisalto_vaihe (
    opetus_id int8 not null,
    vaihe_id int8 not null,
    vaihe_order int4 not null,
    primary key (opetus_id, vaihe_order)
);

create table aipe_opetuksensisalto_vaihe_AUD (
    REV int4 not null,
    opetus_id int8 not null,
    vaihe_id int8 not null,
    vaihe_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, opetus_id, vaihe_id, vaihe_order)
);

create table aipeoppiaine_aipekurssi (
    oppiaine_id int8,
    kurssi_id int8 not null,
    kurssit_order int4 not null,
    primary key (oppiaine_id, kurssit_order)
);

create table aipeoppiaine_aipekurssi_AUD (
    kurssi_id int8 not null,
    REV int4 not null,
    oppiaine_id int8,
    kurssit_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, kurssi_id, kurssit_order)
);

create table aipeoppiaine_aipeoppiaine (
    oppiaine_id int8,
    oppimaara_id int8 not null,
    oppimaara_order int4 not null,
    primary key (oppiaine_id, oppimaara_order)
);

create table aipeoppiaine_aipeoppiaine_AUD (
    oppimaara_id int8 not null,
    REV int4 not null,
    oppiaine_id int8,
    oppimaara_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, oppimaara_id, oppimaara_order)
);

create table aipevaihe_aipeoppiaine (
    vaihe_id int8 not null,
    oppiaine_id int8 not null,
    oppiaine_order int4 not null,
    primary key (vaihe_id, oppiaine_order)
);

create table aipevaihe_aipeoppiaine_AUD (
    REV int4 not null,
    vaihe_id int8 not null,
    oppiaine_id int8 not null,
    oppiaine_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, vaihe_id, oppiaine_id, oppiaine_order)
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

create table yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen (
    yl_aipe_opetuksensisalto_id int8 not null,
    laajaalaisetosaamiset_id int8 not null,
    primary key (yl_aipe_opetuksensisalto_id, laajaalaisetosaamiset_id)
);

create table yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen_AUD (
    REV int4 not null,
    yl_aipe_opetuksensisalto_id int8 not null,
    laajaalaisetosaamiset_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, yl_aipe_opetuksensisalto_id, laajaalaisetosaamiset_id)
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
    ohjaus_id int8,
    pakollinenKurssiKuvaus_id int8,
    sisaltoalueinfo_id int8,
    soveltava_kurssi_kuvaus int8,
    syventava_kurssi_kuvaus int8,
    tehtava_id int8,
    tyotavat_id int8,
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
    ohjaus_id int8,
    pakollinenKurssiKuvaus_id int8,
    sisaltoalueinfo_id int8,
    soveltava_kurssi_kuvaus int8,
    syventava_kurssi_kuvaus int8,
    tehtava_id int8,
    tyotavat_id int8,
    primary key (id, REV)
);

create table yl_aipe_oppiaine_yl_keskeinen_sisaltoalue (
    yl_aipe_oppiaine_id int8 not null,
    sisaltoalueet_id int8 not null,
    sisaltoalueet_ORDER int4 not null,
    primary key (yl_aipe_oppiaine_id, sisaltoalueet_ORDER)
);

create table yl_aipe_oppiaine_yl_keskeinen_sisaltoalue_AUD (
    REV int4 not null,
    yl_aipe_oppiaine_id int8 not null,
    sisaltoalueet_id int8 not null,
    sisaltoalueet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, yl_aipe_oppiaine_id, sisaltoalueet_id, sisaltoalueet_ORDER)
);

create table yl_aipe_oppiaine_yl_opetuksen_tavoite (
    yl_aipe_oppiaine_id int8 not null,
    tavoitteet_id int8 not null,
    tavoitteet_ORDER int4 not null,
    primary key (yl_aipe_oppiaine_id, tavoitteet_ORDER)
);

create table yl_aipe_oppiaine_yl_opetuksen_tavoite_AUD (
    REV int4 not null,
    yl_aipe_oppiaine_id int8 not null,
    tavoitteet_id int8 not null,
    tavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, yl_aipe_oppiaine_id, tavoitteet_id, tavoitteet_ORDER)
);

create table yl_aipe_oppiaineen_yl_opetuksen_tavoite (
    tavoitteet_id int8 not null,
    oppiaine_id int8 not null,
    primary key (tavoitteet_id, oppiaine_id)
);

create table yl_aipe_vaihe (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tunniste uuid not null,
    nimi_id int8,
    paikallisestiPaatettavatAsiat_id int8,
    siirtymaEdellisesta_id int8,
    siirtymaSeuraavaan_id int8,
    tehtava_id int8,
    primary key (id)
);

create table yl_aipe_vaihe_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tunniste uuid,
    nimi_id int8,
    paikallisestiPaatettavatAsiat_id int8,
    siirtymaEdellisesta_id int8,
    siirtymaSeuraavaan_id int8,
    tehtava_id int8,
    primary key (id, REV)
);

alter table aipe_opetuksensisalto_vaihe 
    add constraint UK_6kc3i7vs9ood4c869ufvfdv39  unique (vaihe_id);

alter table aipeoppiaine_aipekurssi 
    add constraint UK_76yth2um3xyrlnx01jrmyexv5  unique (kurssi_id);

alter table aipeoppiaine_aipeoppiaine 
    add constraint UK_1np73635586vpqmknf5cust0q  unique (oppimaara_id);

alter table aipevaihe_aipeoppiaine 
    add constraint UK_2fd8ukogha0u8ig909ju4k4gy  unique (oppiaine_id);

alter table public.yl_aipe_kurssi 
    add constraint UK_fmby2ha9jh1xhynavvpi08mpj  unique (tunniste);

alter table yl_aipe_opetuksensisalto 
    add constraint UK_4nqcxldr6s0h4ri5reoeb7u91  unique (peruste_id);

alter table yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen 
    add constraint UK_3xwa2t6fvoeptgu3aoeuuei5s  unique (laajaalaisetosaamiset_id);

alter table yl_aipe_oppiaine_yl_keskeinen_sisaltoalue 
    add constraint UK_conoek14yr6s9m2cdafax85h4  unique (sisaltoalueet_id);

alter table yl_aipe_oppiaine_yl_opetuksen_tavoite 
    add constraint UK_39x1j2aa3fu5xefgybs9bann1  unique (tavoitteet_id);

alter table aipe_opetuksensisalto_vaihe 
    add constraint FK_6kc3i7vs9ood4c869ufvfdv39 
    foreign key (vaihe_id) 
    references yl_aipe_vaihe;

alter table aipe_opetuksensisalto_vaihe 
    add constraint FK_1va2meeveb31kolhsr2opgfvj 
    foreign key (opetus_id) 
    references yl_aipe_opetuksensisalto;

alter table aipe_opetuksensisalto_vaihe_AUD 
    add constraint FK_innno1d96hqgbe6mlcxdaii83 
    foreign key (REV) 
    references revinfo;

alter table aipe_opetuksensisalto_vaihe_AUD 
    add constraint FK_58ldddje5tdsxxcx07229v7h5 
    foreign key (REVEND) 
    references revinfo;

alter table aipeoppiaine_aipekurssi 
    add constraint FK_shy01shno4dvjom2n5605bicp 
    foreign key (oppiaine_id) 
    references yl_aipe_oppiaine;

alter table aipeoppiaine_aipekurssi 
    add constraint FK_76yth2um3xyrlnx01jrmyexv5 
    foreign key (kurssi_id) 
    references public.yl_aipe_kurssi;

alter table aipeoppiaine_aipekurssi_AUD 
    add constraint FK_dkg4s0i4reqbsiqvvjypsts7 
    foreign key (kurssi_id, REV) 
    references public.yl_aipe_kurssi_AUD;

alter table aipeoppiaine_aipekurssi_AUD 
    add constraint FK_liacyfrp88o3ujjdyi3qbva7n 
    foreign key (REV) 
    references revinfo;

alter table aipeoppiaine_aipekurssi_AUD 
    add constraint FK_6phdbndm3hi6uxf9knn9ax4tv 
    foreign key (REVEND) 
    references revinfo;

alter table aipeoppiaine_aipeoppiaine 
    add constraint FK_s6f0989y6d53xsvttxvh0eqf2 
    foreign key (oppiaine_id) 
    references yl_aipe_oppiaine;

alter table aipeoppiaine_aipeoppiaine 
    add constraint FK_1np73635586vpqmknf5cust0q 
    foreign key (oppimaara_id) 
    references yl_aipe_oppiaine;

alter table aipeoppiaine_aipeoppiaine_AUD 
    add constraint FK_4cj5o22o1pjw4rle7einjaexv 
    foreign key (oppimaara_id, REV) 
    references yl_aipe_oppiaine_AUD;

alter table aipeoppiaine_aipeoppiaine_AUD 
    add constraint FK_svwcknog5x0qmy9gqfu1u80tc 
    foreign key (REV) 
    references revinfo;

alter table aipeoppiaine_aipeoppiaine_AUD 
    add constraint FK_17yjjh4th0nka7t1923fqc9o4 
    foreign key (REVEND) 
    references revinfo;

alter table aipevaihe_aipeoppiaine 
    add constraint FK_2fd8ukogha0u8ig909ju4k4gy 
    foreign key (oppiaine_id) 
    references yl_aipe_oppiaine;

alter table aipevaihe_aipeoppiaine 
    add constraint FK_81v3b6cfhipoqhpyc2lhab93i 
    foreign key (vaihe_id) 
    references yl_aipe_vaihe;

alter table aipevaihe_aipeoppiaine_AUD 
    add constraint FK_kv4aoamd27ufoxftdl1xa71td 
    foreign key (REV) 
    references revinfo;

alter table aipevaihe_aipeoppiaine_AUD 
    add constraint FK_1gxtaq02cpe9e780y7r0ss4vm 
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

alter table yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen 
    add constraint FK_3xwa2t6fvoeptgu3aoeuuei5s 
    foreign key (laajaalaisetosaamiset_id) 
    references yl_laajaalainen_osaaminen;

alter table yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen 
    add constraint FK_r73rd51fircbcolqqsxx3n2wu 
    foreign key (yl_aipe_opetuksensisalto_id) 
    references yl_aipe_opetuksensisalto;

alter table yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen_AUD 
    add constraint FK_h1p17v1ud1rgkh6uoyhhegcxp 
    foreign key (REV) 
    references revinfo;

alter table yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen_AUD 
    add constraint FK_ma5lle3cw3csa0qailxtj22ua 
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
    add constraint FK_rpho5tomeeb6qfyrxb6g8i713 
    foreign key (ohjaus_id) 
    references yl_tekstiosa;

alter table yl_aipe_oppiaine 
    add constraint FK_7d4bkp06j5c7uhhlvn7ptcbng 
    foreign key (pakollinenKurssiKuvaus_id) 
    references tekstipalanen;

alter table yl_aipe_oppiaine 
    add constraint FK_625wsodh2ol4fbrqa427i4xv6 
    foreign key (sisaltoalueinfo_id) 
    references yl_tekstiosa;

alter table yl_aipe_oppiaine 
    add constraint FK_2k2mhx3bp31gu28740xcobxhs 
    foreign key (soveltava_kurssi_kuvaus) 
    references tekstipalanen;

alter table yl_aipe_oppiaine 
    add constraint FK_78a5otc7obbip2hkjnwtut3n2 
    foreign key (syventava_kurssi_kuvaus) 
    references tekstipalanen;

alter table yl_aipe_oppiaine 
    add constraint FK_pa711ku4dcdattotbvdjtues0 
    foreign key (tehtava_id) 
    references yl_tekstiosa;

alter table yl_aipe_oppiaine 
    add constraint FK_bu4g0o061k7cx39qm1nir5432 
    foreign key (tyotavat_id) 
    references yl_tekstiosa;

alter table yl_aipe_oppiaine_AUD 
    add constraint FK_ataxn38vn07dpes9nyomvdxuy 
    foreign key (REV) 
    references revinfo;

alter table yl_aipe_oppiaine_AUD 
    add constraint FK_c24ccjukc9bcs89s1o5vn8n0 
    foreign key (REVEND) 
    references revinfo;

alter table yl_aipe_oppiaine_yl_keskeinen_sisaltoalue 
    add constraint FK_conoek14yr6s9m2cdafax85h4 
    foreign key (sisaltoalueet_id) 
    references yl_keskeinen_sisaltoalue;

alter table yl_aipe_oppiaine_yl_keskeinen_sisaltoalue 
    add constraint FK_tqhfr4igkmo3h4nwo67q46pq1 
    foreign key (yl_aipe_oppiaine_id) 
    references yl_aipe_oppiaine;

alter table yl_aipe_oppiaine_yl_keskeinen_sisaltoalue_AUD 
    add constraint FK_fbqh2e326qqat831b6l7b4vwt 
    foreign key (REV) 
    references revinfo;

alter table yl_aipe_oppiaine_yl_keskeinen_sisaltoalue_AUD 
    add constraint FK_m7qa5mlge00i0vxq1uxw7yvwi 
    foreign key (REVEND) 
    references revinfo;

alter table yl_aipe_oppiaine_yl_opetuksen_tavoite 
    add constraint FK_39x1j2aa3fu5xefgybs9bann1 
    foreign key (tavoitteet_id) 
    references yl_opetuksen_tavoite;

alter table yl_aipe_oppiaine_yl_opetuksen_tavoite 
    add constraint FK_am1ojsr4q80kbg4qta1b2peo0 
    foreign key (yl_aipe_oppiaine_id) 
    references yl_aipe_oppiaine;

alter table yl_aipe_oppiaine_yl_opetuksen_tavoite_AUD 
    add constraint FK_lqurbly3x95llrh3ar7dk2y80 
    foreign key (REV) 
    references revinfo;

alter table yl_aipe_oppiaine_yl_opetuksen_tavoite_AUD 
    add constraint FK_spajqjdes73d9nkaw4980fyih 
    foreign key (REVEND) 
    references revinfo;

alter table yl_aipe_oppiaineen_yl_opetuksen_tavoite 
    add constraint FK_7pyy0bpkgf85jhs2alph0o67h 
    foreign key (oppiaine_id) 
    references yl_aipe_oppiaine;

alter table yl_aipe_oppiaineen_yl_opetuksen_tavoite 
    add constraint FK_mts869f539h04m3a5xei4p57w 
    foreign key (tavoitteet_id) 
    references yl_opetuksen_tavoite;

alter table yl_aipe_vaihe 
    add constraint FK_2se6u5wn2fe6xu5d0letsq1ws 
    foreign key (nimi_id) 
    references tekstipalanen;

alter table yl_aipe_vaihe 
    add constraint FK_5uc5g60ctktkeu04yrwqbkse5 
    foreign key (paikallisestiPaatettavatAsiat_id) 
    references yl_tekstiosa;

alter table yl_aipe_vaihe 
    add constraint FK_ogfng6qkp3sqko6qlkxhjc0u5 
    foreign key (siirtymaEdellisesta_id) 
    references yl_tekstiosa;

alter table yl_aipe_vaihe 
    add constraint FK_6ykow5emmsxoe6s4era619ga6 
    foreign key (siirtymaSeuraavaan_id) 
    references yl_tekstiosa;

alter table yl_aipe_vaihe 
    add constraint FK_2u3ft6ld0o9p4gv5ktjl2b6aw 
    foreign key (tehtava_id) 
    references yl_tekstiosa;

alter table yl_aipe_vaihe_AUD 
    add constraint FK_rajwj5tu06oc8pb0chvnvrahs 
    foreign key (REV) 
    references revinfo;

alter table yl_aipe_vaihe_AUD 
    add constraint FK_qxe806d1krhtmxgs4q9e26ine 
    foreign key (REVEND) 
    references revinfo;


