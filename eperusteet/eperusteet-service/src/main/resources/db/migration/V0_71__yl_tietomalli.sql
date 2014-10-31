create table yl_keskeinen_sisaltoalue (
  id int8 not null,
  kuvaus_id int8,
  nimi_id int8,
  primary key (id)
);

create table yl_keskeinen_sisaltoalue_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  kuvaus_id int8,
  nimi_id int8,
  primary key (id, REV)
);

create table yl_laajaalainen_osaaminen (
  id int8 not null,
  kuvaus_id int8,
  nimi_id int8,
  primary key (id)
);

create table yl_laajaalainen_osaaminen_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  kuvaus_id int8,
  nimi_id int8,
  primary key (id, REV)
);

create table yl_opetuksen_tavoite (
  id int8 not null,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  tavoite_id int8,
  primary key (id)
);

create table yl_opetuksen_tavoite_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  tavoite_id int8,
  primary key (id, REV)
);

create table yl_opetuksen_tavoite_yl_keskeinen_sisaltoalue (
  yl_opetuksen_tavoite_id int8 not null,
  sisaltoalueet_id int8 not null,
  primary key (yl_opetuksen_tavoite_id, sisaltoalueet_id)
);

create table yl_opetuksen_tavoite_yl_keskeinen_sisaltoalue_AUD (
  REV int4 not null,
  yl_opetuksen_tavoite_id int8 not null,
  sisaltoalueet_id int8 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, yl_opetuksen_tavoite_id, sisaltoalueet_id)
);

create table yl_opetuksen_tavoite_yl_laajaalainen_osaaminen (
  yl_opetuksen_tavoite_id int8 not null,
  laajattavoitteet_id int8 not null,
  primary key (yl_opetuksen_tavoite_id, laajattavoitteet_id)
);

create table yl_opetuksen_tavoite_yl_laajaalainen_osaaminen_AUD (
  REV int4 not null,
  yl_opetuksen_tavoite_id int8 not null,
  laajattavoitteet_id int8 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, yl_opetuksen_tavoite_id, laajattavoitteet_id)
);

create table yl_opetuksen_tavoite_yl_tavoitteen_arviointi (
  yl_opetuksen_tavoite_id int8 not null,
  arvioinninKohteet_id int8 not null,
  primary key (yl_opetuksen_tavoite_id, arvioinninKohteet_id)
);

create table yl_opetuksen_tavoite_yl_tavoitteen_arviointi_AUD (
  REV int4 not null,
  yl_opetuksen_tavoite_id int8 not null,
  arvioinninKohteet_id int8 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, yl_opetuksen_tavoite_id, arvioinninKohteet_id)
);

create table yl_oppiaine (
  id int8 not null,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  koosteinen boolean not null,
  nimi_id int8,
  oppiaine_id int8,
  tehtava_id int8,
  primary key (id)
);

create table yl_oppiaine_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  koosteinen boolean,
  nimi_id int8,
  oppiaine_id int8,
  tehtava_id int8,
  primary key (id, REV)
);

create table yl_oppiaineen_vlkok (
  id int8 not null,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  arviointi_id int8,
  ohjaus_id int8,
  oppiaine_id int8 not null,
  tehtava_id int8,
  tyotavat_id int8,
  vuosiluokkaKokonaisuus_id int8 not null,
  primary key (id)
);

create table yl_oppiaineen_vlkok_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  arviointi_id int8,
  ohjaus_id int8,
  oppiaine_id int8,
  tehtava_id int8,
  tyotavat_id int8,
  vuosiluokkaKokonaisuus_id int8,
  primary key (id, REV)
);

create table yl_oppiaineen_vlkok_yl_keskeinen_sisaltoalue (
  yl_oppiaineen_vlkok_id int8 not null,
  sisaltoAlueet_id int8 not null,
  sisaltoAlueet_ORDER int4 not null,
  primary key (yl_oppiaineen_vlkok_id, sisaltoAlueet_ORDER)
);

create table yl_oppiaineen_vlkok_yl_keskeinen_sisaltoalue_AUD (
  REV int4 not null,
  yl_oppiaineen_vlkok_id int8 not null,
  sisaltoAlueet_id int8 not null,
  sisaltoAlueet_ORDER int4 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, yl_oppiaineen_vlkok_id, sisaltoAlueet_id, sisaltoAlueet_ORDER)
);

create table yl_oppiaineen_vlkok_yl_opetuksen_tavoite (
  yl_oppiaineen_vlkok_id int8 not null,
  tavoitteet_id int8 not null,
  tavoitteet_ORDER int4 not null,
  primary key (yl_oppiaineen_vlkok_id, tavoitteet_ORDER)
);

create table yl_oppiaineen_vlkok_yl_opetuksen_tavoite_AUD (
  REV int4 not null,
  yl_oppiaineen_vlkok_id int8 not null,
  tavoitteet_id int8 not null,
  tavoitteet_ORDER int4 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, yl_oppiaineen_vlkok_id, tavoitteet_id, tavoitteet_ORDER)
);

create table yl_perusop_perusteen_sisalto (
  id int8 not null,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  peruste_id int8 not null,
  sisalto_id int8,
  primary key (id)
);

create table yl_perusop_perusteen_sisalto_AUD (
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

create table yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen (
  yl_perusop_perusteen_sisalto_id int8 not null,
  laajaAlalaisetOsaamiset_id int8 not null,
  primary key (yl_perusop_perusteen_sisalto_id, laajaAlalaisetOsaamiset_id)
);

create table yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen_AUD (
  REV int4 not null,
  yl_perusop_perusteen_sisalto_id int8 not null,
  laajaAlalaisetOsaamiset_id int8 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, yl_perusop_perusteen_sisalto_id, laajaAlalaisetOsaamiset_id)
);

create table yl_perusop_perusteen_sisalto_yl_oppiaine (
  yl_perusop_perusteen_sisalto_id int8 not null,
  oppiaineet_id int8 not null,
  primary key (yl_perusop_perusteen_sisalto_id, oppiaineet_id)
);

create table yl_perusop_perusteen_sisalto_yl_oppiaine_AUD (
  REV int4 not null,
  yl_perusop_perusteen_sisalto_id int8 not null,
  oppiaineet_id int8 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, yl_perusop_perusteen_sisalto_id, oppiaineet_id)
);

create table yl_perusop_perusteen_sisalto_yl_vlkokonaisuus (
  yl_perusop_perusteen_sisalto_id int8 not null,
  vuosiluokkakokonaisuudet_id int8 not null,
  primary key (yl_perusop_perusteen_sisalto_id, vuosiluokkakokonaisuudet_id)
);

create table yl_perusop_perusteen_sisalto_yl_vlkokonaisuus_AUD (
  REV int4 not null,
  yl_perusop_perusteen_sisalto_id int8 not null,
  vuosiluokkakokonaisuudet_id int8 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, yl_perusop_perusteen_sisalto_id, vuosiluokkakokonaisuudet_id)
);

create table yl_tavoitteen_arviointi (
  id int8 not null,
  arvioinninKohde_id int8,
  hyvanOsaamisenKuvaus_id int8,
  primary key (id)
);

create table yl_tavoitteen_arviointi_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  arvioinninKohde_id int8,
  hyvanOsaamisenKuvaus_id int8,
  primary key (id, REV)
);

create table yl_tekstiosa (
  id int8 not null,
  otsikko_id int8,
  teksti_id int8,
  primary key (id)
);

create table yl_tekstiosa_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  otsikko_id int8,
  teksti_id int8,
  primary key (id, REV)
);

create table yl_vlkok_laaja_osaaminen (
  id int8 not null,
  kuvaus_id int8,
  laajaalainenOsaaminen_id int8 not null,
  vuosiluokkaKokonaisuus_id int8 not null,
  primary key (id)
);

create table yl_vlkok_laaja_osaaminen_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  kuvaus_id int8,
  laajaalainenOsaaminen_id int8,
  vuosiluokkaKokonaisuus_id int8,
  primary key (id, REV)
);

create table yl_vlkokonaisuus (
  id int8 not null,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  nimi_id int8,
  tehtava_id int8,
  primary key (id)
);

create table yl_vlkokonaisuus_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  nimi_id int8,
  tehtava_id int8,
  primary key (id, REV)
);

alter table yl_opetuksen_tavoite_yl_tavoitteen_arviointi
add constraint UK_937uk98kimlo06kxeqw5tlh5g  unique (arvioinninKohteet_id);

alter table yl_oppiaineen_vlkok_yl_keskeinen_sisaltoalue
add constraint UK_amphrb2ih4iu3e6qeg1qh2c47  unique (sisaltoAlueet_id);

alter table yl_oppiaineen_vlkok_yl_opetuksen_tavoite
add constraint UK_d7wmimjt50pfqhy7yutn9fpeo  unique (tavoitteet_id);

alter table yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen
add constraint UK_cajr740x8h476iwyek65f497p  unique (laajaAlalaisetOsaamiset_id);

alter table yl_perusop_perusteen_sisalto_yl_oppiaine
add constraint UK_1sq9h0pw3u8a22tflfwpb11h8  unique (oppiaineet_id);

alter table yl_perusop_perusteen_sisalto_yl_vlkokonaisuus
add constraint UK_c0a0qxisymqr50ljrtplld672  unique (vuosiluokkakokonaisuudet_id);

alter table yl_keskeinen_sisaltoalue
add constraint FK_dg461l7aa94bpdbqlfliqubja
foreign key (kuvaus_id)
references tekstipalanen;

alter table yl_keskeinen_sisaltoalue
add constraint FK_4erhvf8nbq2yam11gmhxjn1lm
foreign key (nimi_id)
references tekstipalanen;

alter table yl_keskeinen_sisaltoalue_AUD
add constraint FK_clvlbryvqpom35h2u1kb7ofl0
foreign key (REV)
references revinfo;

alter table yl_keskeinen_sisaltoalue_AUD
add constraint FK_6v5qlgvo5rihwhb0qhwyvog3o
foreign key (REVEND)
references revinfo;

alter table yl_laajaalainen_osaaminen
add constraint FK_2x6c2iub8igffqxg0sqkemd73
foreign key (kuvaus_id)
references tekstipalanen;

alter table yl_laajaalainen_osaaminen
add constraint FK_h7n37afu3uqq9ddnq4h471po4
foreign key (nimi_id)
references tekstipalanen;

alter table yl_laajaalainen_osaaminen_AUD
add constraint FK_2v8b6t2v86vrrxryiiyx1icx1
foreign key (REV)
references revinfo;

alter table yl_laajaalainen_osaaminen_AUD
add constraint FK_d7rm84ovkuw65irfgy70bf779
foreign key (REVEND)
references revinfo;

alter table yl_opetuksen_tavoite
add constraint FK_erj8k5kk5wmxpbw4mcm73hqav
foreign key (tavoite_id)
references tekstipalanen;

alter table yl_opetuksen_tavoite_AUD
add constraint FK_juh4tq5gbhk0rua92pghrpalb
foreign key (REV)
references revinfo;

alter table yl_opetuksen_tavoite_AUD
add constraint FK_qtt26fdtlck00v87u5ng71c3e
foreign key (REVEND)
references revinfo;

alter table yl_opetuksen_tavoite_yl_keskeinen_sisaltoalue
add constraint FK_hx24iacm60h1buw29qvkq0fhd
foreign key (sisaltoalueet_id)
references yl_keskeinen_sisaltoalue;

alter table yl_opetuksen_tavoite_yl_keskeinen_sisaltoalue
add constraint FK_dq3gk7j71hkmabk370kif3ci6
foreign key (yl_opetuksen_tavoite_id)
references yl_opetuksen_tavoite;

alter table yl_opetuksen_tavoite_yl_keskeinen_sisaltoalue_AUD
add constraint FK_tovb3g5l422clmmtgyosul92m
foreign key (REV)
references revinfo;

alter table yl_opetuksen_tavoite_yl_keskeinen_sisaltoalue_AUD
add constraint FK_dvhscsjemyomtx83lnsoxqkpo
foreign key (REVEND)
references revinfo;

alter table yl_opetuksen_tavoite_yl_laajaalainen_osaaminen
add constraint FK_pbxllmonwxumuait6hiub1lx6
foreign key (laajattavoitteet_id)
references yl_laajaalainen_osaaminen;

alter table yl_opetuksen_tavoite_yl_laajaalainen_osaaminen
add constraint FK_h3v1utg727fxcmh1mu0mu29v9
foreign key (yl_opetuksen_tavoite_id)
references yl_opetuksen_tavoite;

alter table yl_opetuksen_tavoite_yl_laajaalainen_osaaminen_AUD
add constraint FK_io304x2st6hxcgequog8pil29
foreign key (REV)
references revinfo;

alter table yl_opetuksen_tavoite_yl_laajaalainen_osaaminen_AUD
add constraint FK_ao2hwidm1nrsexm1n2actnyel
foreign key (REVEND)
references revinfo;

alter table yl_opetuksen_tavoite_yl_tavoitteen_arviointi
add constraint FK_937uk98kimlo06kxeqw5tlh5g
foreign key (arvioinninKohteet_id)
references yl_tavoitteen_arviointi;

alter table yl_opetuksen_tavoite_yl_tavoitteen_arviointi
add constraint FK_adjl9kh7mtqp93dk4td6scidy
foreign key (yl_opetuksen_tavoite_id)
references yl_opetuksen_tavoite;

alter table yl_opetuksen_tavoite_yl_tavoitteen_arviointi_AUD
add constraint FK_2sc7jfaxeylk763ybartqg8h2
foreign key (REV)
references revinfo;

alter table yl_opetuksen_tavoite_yl_tavoitteen_arviointi_AUD
add constraint FK_3jd8j5ui1xb6frbniln5f20cy
foreign key (REVEND)
references revinfo;

alter table yl_oppiaine
add constraint FK_rx8pq8h4xqbgmge8hf29b03c0
foreign key (nimi_id)
references tekstipalanen;

alter table yl_oppiaine
add constraint FK_16n8oqxikvlu1i6rw9blfbwh3
foreign key (oppiaine_id)
references yl_oppiaine;

alter table yl_oppiaine
add constraint FK_moxy4ld9vlrfvmiqoi3h9v2b6
foreign key (tehtava_id)
references yl_tekstiosa;

alter table yl_oppiaine_AUD
add constraint FK_dv7vnqmq3wv9wgvgne6brwtcm
foreign key (REV)
references revinfo;

alter table yl_oppiaine_AUD
add constraint FK_g3379bx6ddw5mtkc9vrg9yxfw
foreign key (REVEND)
references revinfo;

alter table yl_oppiaineen_vlkok
add constraint FK_wmtp10gy09mk66odtatg9uf
foreign key (arviointi_id)
references yl_tekstiosa;

alter table yl_oppiaineen_vlkok
add constraint FK_b5d1ankpwvgud5q1bxj3porsa
foreign key (ohjaus_id)
references yl_tekstiosa;

alter table yl_oppiaineen_vlkok
add constraint FK_mltteh2vpjxm0ke9lmcwitide
foreign key (oppiaine_id)
references yl_oppiaine;

alter table yl_oppiaineen_vlkok
add constraint FK_duf1ftqdkxrvrxacdtclchdcu
foreign key (tehtava_id)
references yl_tekstiosa;

alter table yl_oppiaineen_vlkok
add constraint FK_ftvtvtajemjo1fw3khsvi1mhh
foreign key (tyotavat_id)
references yl_tekstiosa;

alter table yl_oppiaineen_vlkok
add constraint FK_knvmyvfp51mmsu431pcnkx6l2
foreign key (vuosiluokkaKokonaisuus_id)
references yl_vlkokonaisuus;

alter table yl_oppiaineen_vlkok_AUD
add constraint FK_k3s5p24je9cvgj72casal30l4
foreign key (REV)
references revinfo;

alter table yl_oppiaineen_vlkok_AUD
add constraint FK_rc8x9r0a3cufktjg5fm44kqrk
foreign key (REVEND)
references revinfo;

alter table yl_oppiaineen_vlkok_yl_keskeinen_sisaltoalue
add constraint FK_amphrb2ih4iu3e6qeg1qh2c47
foreign key (sisaltoAlueet_id)
references yl_keskeinen_sisaltoalue;

alter table yl_oppiaineen_vlkok_yl_keskeinen_sisaltoalue
add constraint FK_l5yqiend42bwd0t14t04t2rq5
foreign key (yl_oppiaineen_vlkok_id)
references yl_oppiaineen_vlkok;

alter table yl_oppiaineen_vlkok_yl_keskeinen_sisaltoalue_AUD
add constraint FK_iclfudvax0m1dsvwyfkv99dc5
foreign key (REV)
references revinfo;

alter table yl_oppiaineen_vlkok_yl_keskeinen_sisaltoalue_AUD
add constraint FK_f930f0hckbo4c73n0tbsqsyw4
foreign key (REVEND)
references revinfo;

alter table yl_oppiaineen_vlkok_yl_opetuksen_tavoite
add constraint FK_d7wmimjt50pfqhy7yutn9fpeo
foreign key (tavoitteet_id)
references yl_opetuksen_tavoite;

alter table yl_oppiaineen_vlkok_yl_opetuksen_tavoite
add constraint FK_psbq6me1kopefwx7hchplg0l6
foreign key (yl_oppiaineen_vlkok_id)
references yl_oppiaineen_vlkok;

alter table yl_oppiaineen_vlkok_yl_opetuksen_tavoite_AUD
add constraint FK_14uokki93aoy21oc9rtbwc9y1
foreign key (REV)
references revinfo;

alter table yl_oppiaineen_vlkok_yl_opetuksen_tavoite_AUD
add constraint FK_o1t80qh87i59oca52sddgrvxu
foreign key (REVEND)
references revinfo;

alter table yl_perusop_perusteen_sisalto
add constraint FK_7tv91i78ycsfag9la42u9mx8q
foreign key (peruste_id)
references peruste;

alter table yl_perusop_perusteen_sisalto
add constraint FK_o6s1mrq9rqrflgjno3e798wjb
foreign key (sisalto_id)
references perusteenosaviite;

alter table yl_perusop_perusteen_sisalto_AUD
add constraint FK_fxuf2n0ei67xknloh7m3977bw
foreign key (REV)
references revinfo;

alter table yl_perusop_perusteen_sisalto_AUD
add constraint FK_6jcy0jmaknqq31xomvn2bne6y
foreign key (REVEND)
references revinfo;

alter table yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen
add constraint FK_cajr740x8h476iwyek65f497p
foreign key (laajaAlalaisetOsaamiset_id)
references yl_laajaalainen_osaaminen;

alter table yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen
add constraint FK_9o4ralfrlkfv9xw3oq0o0pkaf
foreign key (yl_perusop_perusteen_sisalto_id)
references yl_perusop_perusteen_sisalto;

alter table yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen_AUD
add constraint FK_31m2r8agmxu5jfkn75h86pwvq
foreign key (REV)
references revinfo;

alter table yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen_AUD
add constraint FK_7woh5tm1hmbvbr0uyg0isldb
foreign key (REVEND)
references revinfo;

alter table yl_perusop_perusteen_sisalto_yl_oppiaine
add constraint FK_1sq9h0pw3u8a22tflfwpb11h8
foreign key (oppiaineet_id)
references yl_oppiaine;

alter table yl_perusop_perusteen_sisalto_yl_oppiaine
add constraint FK_esba4w80vuul25kf9xlv6bhvx
foreign key (yl_perusop_perusteen_sisalto_id)
references yl_perusop_perusteen_sisalto;

alter table yl_perusop_perusteen_sisalto_yl_oppiaine_AUD
add constraint FK_aqc9bbq318d34mif0crcrcdeb
foreign key (REV)
references revinfo;

alter table yl_perusop_perusteen_sisalto_yl_oppiaine_AUD
add constraint FK_448ft8f337j3egwach8mgqsqc
foreign key (REVEND)
references revinfo;

alter table yl_perusop_perusteen_sisalto_yl_vlkokonaisuus
add constraint FK_c0a0qxisymqr50ljrtplld672
foreign key (vuosiluokkakokonaisuudet_id)
references yl_vlkokonaisuus;

alter table yl_perusop_perusteen_sisalto_yl_vlkokonaisuus
add constraint FK_rdqhx4mf30y19idrd2xusggy9
foreign key (yl_perusop_perusteen_sisalto_id)
references yl_perusop_perusteen_sisalto;

alter table yl_perusop_perusteen_sisalto_yl_vlkokonaisuus_AUD
add constraint FK_j4x23v99h2ai4wscxq8cs5620
foreign key (REV)
references revinfo;

alter table yl_perusop_perusteen_sisalto_yl_vlkokonaisuus_AUD
add constraint FK_ru833370ofx4h9nam0xu331hr
foreign key (REVEND)
references revinfo;

alter table yl_tavoitteen_arviointi
add constraint FK_j4b3uq5loa5f0vxagqg77iy8e
foreign key (arvioinninKohde_id)
references tekstipalanen;

alter table yl_tavoitteen_arviointi
add constraint FK_hdxnaoxbcyv8eq150oykyx0my
foreign key (hyvanOsaamisenKuvaus_id)
references tekstipalanen;

alter table yl_tavoitteen_arviointi_AUD
add constraint FK_cfymwdxddwj3xhydnnbgkh15p
foreign key (REV)
references revinfo;

alter table yl_tavoitteen_arviointi_AUD
add constraint FK_2bxlbifjhlv0dcs9jdw129dsj
foreign key (REVEND)
references revinfo;

alter table yl_tekstiosa
add constraint FK_som0idq9425f68uj4oqoujgcj
foreign key (otsikko_id)
references tekstipalanen;

alter table yl_tekstiosa
add constraint FK_7vu9tu0q3duqvvft3pgc6km24
foreign key (teksti_id)
references tekstipalanen;

alter table yl_tekstiosa_AUD
add constraint FK_es3u1n2p6fufl8fjjfq62rjxe
foreign key (REV)
references revinfo;

alter table yl_tekstiosa_AUD
add constraint FK_m3tr8t6pr00s5vhufcpwwol3e
foreign key (REVEND)
references revinfo;

alter table yl_vlkok_laaja_osaaminen
add constraint FK_7o5lm8ep2hci1wn5kbnp2hctn
foreign key (kuvaus_id)
references tekstipalanen;

alter table yl_vlkok_laaja_osaaminen
add constraint FK_asjpvj64ojmgycsr6i35g32th
foreign key (laajaalainenOsaaminen_id)
references yl_laajaalainen_osaaminen;

alter table yl_vlkok_laaja_osaaminen
add constraint FK_2ssp7eaqg4os0b38a1vig3dhk
foreign key (vuosiluokkaKokonaisuus_id)
references yl_vlkokonaisuus;

alter table yl_vlkok_laaja_osaaminen_AUD
add constraint FK_hbun2kyb65qhbopgeyp4rdq3k
foreign key (REV)
references revinfo;

alter table yl_vlkok_laaja_osaaminen_AUD
add constraint FK_3ni73ogypmx1utnulr0jvhwsv
foreign key (REVEND)
references revinfo;

alter table yl_vlkokonaisuus
add constraint FK_hol2jsfu944wto76d2w504k8y
foreign key (nimi_id)
references tekstipalanen;

alter table yl_vlkokonaisuus
add constraint FK_t772fdsbo5v9ikg2ot8fdwoa
foreign key (tehtava_id)
references yl_tekstiosa;

alter table yl_vlkokonaisuus_AUD
add constraint FK_rjqv3kongp053wkby2e590m1n
foreign key (REV)
references revinfo;

alter table yl_vlkokonaisuus_AUD
add constraint FK_kwxmv85avguwmvtcji810oq7p
foreign key (REVEND)
references revinfo;
