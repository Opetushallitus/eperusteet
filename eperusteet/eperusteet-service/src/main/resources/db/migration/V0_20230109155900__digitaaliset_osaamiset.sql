create table digitaalinenosaaminen_perusteen_sisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8 not null,
    sisalto_id int8,
    primary key (id)
);

create table digitaalinenosaaminen_perusteen_sisalto_AUD (
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

alter table digitaalinenosaaminen_perusteen_sisalto
    add constraint FK_1luja347osx8n614se9xck25m
    foreign key (peruste_id)
    references peruste;

alter table digitaalinenosaaminen_perusteen_sisalto
    add constraint FK_2wq4f86qg1iw5bhuvhc47he51
    foreign key (sisalto_id)
    references perusteenosaviite;

alter table digitaalinenosaaminen_perusteen_sisalto_AUD
    add constraint FK_2eppp8blqx6itfclp811p5vvk
    foreign key (REV)
    references revinfo;

alter table digitaalinenosaaminen_perusteen_sisalto_AUD
    add constraint FK_dcigftkt7rvyfq45np7d0ggp2
    foreign key (REVEND)
    references revinfo;

CREATE OR REPLACE VIEW perusteenosa_projekti AS WITH RECURSIVE vanhemmat AS (
  SELECT pov.id, pov.vanhempi_id, pov.perusteenosa_id
    FROM perusteenosaviite pov, perusteenosa po
    WHERE pov.perusteenosa_id = po.id
  UNION ALL
    SELECT pov.id, pov.vanhempi_id, v.perusteenosa_id
      FROM perusteenosaviite pov, vanhemmat v
      WHERE pov.id = v.vanhempi_id
)

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     digitaalinenosaaminen_perusteen_sisalto s ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     tutkintoonvalmentava_perusteen_sisalto s ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     vapaasivistystyo_perusteen_sisalto s ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     tpo_perusteen_sisalto s ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     suoritustapa s ,
     peruste_suoritustapa ps ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_perusteenosaviite_id = v.id
  AND ps.suoritustapa_id = s.id
  AND ps.peruste_id = p.id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     yl_perusop_perusteen_sisalto s ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     yl_aipe_opetuksensisalto s ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     esiop_perusteen_sisalto s ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     opas_sisalto s,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM vanhemmat v ,
     yl_lukiokoulutuksen_perusteen_sisalto s ,
     peruste p ,
     perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT v.perusteenosa_id AS id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM
  vanhemmat v,
  yl_lops2019_sisalto s,
  peruste p,
  perusteprojekti pp
WHERE s.sisalto_id = v.id
  AND p.id = s.peruste_id
  AND pp.peruste_id = p.id
UNION

SELECT DISTINCT po.id,
                pp.id AS perusteprojekti_id,
                pp.ryhmaoid,
                pp.tila,
                pp.esikatseltavissa
FROM perusteenosa po ,
     tutkinnonosaviite v ,
     suoritustapa s ,
     peruste_suoritustapa ps ,
     peruste p ,
     perusteprojekti pp
WHERE po.id = v.tutkinnonosa_id
  AND v.suoritustapa_id = s.id
  AND ps.suoritustapa_id = s.id
  AND ps.peruste_id = p.id
  AND pp.peruste_id = p.id;

create table osaamiskokonaisuus (
    muokattu timestamp,
    id int8 not null,
    kuvaus_id int8,
    primary key (id)
);

create table osaamiskokonaisuus_AUD (
    id int8 not null,
    REV int4 not null,
    muokattu timestamp,
    kuvaus_id int8,
    primary key (id, REV)
);

create table osaamiskokonaisuus_kasitteisto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    taso varchar(255) not null,
    keskeinenKasitteisto_id int8,
    kuvaus_id int8,
    primary key (id)
);

create table osaamiskokonaisuus_kasitteisto_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    taso varchar(255),
    keskeinenKasitteisto_id int8,
    kuvaus_id int8,
    primary key (id, REV)
);

create table osaamiskokonaisuus_kasitteisto_join (
    osaamiskokonaisuus_id int8 not null,
    kasitteistot_id int8 not null,
    kasitteistot_ORDER int4 not null,
    primary key (osaamiskokonaisuus_id, kasitteistot_ORDER)
);

create table osaamiskokonaisuus_kasitteisto_join_AUD (
    REV int4 not null,
    osaamiskokonaisuus_id int8 not null,
    kasitteistot_id int8 not null,
    kasitteistot_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaamiskokonaisuus_id, kasitteistot_id, kasitteistot_ORDER)
);

create table osaamiskokonaisuus_osa_alue (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    primary key (id)
);

create table osaamiskokonaisuus_osa_alue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    primary key (id, REV)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaukset_join (
    osaamiskokonaisuus_osa_alue_id int8 not null,
    tasokuvaukset_id int8 not null,
    tasokuvaukset_ORDER int4 not null,
    primary key (osaamiskokonaisuus_osa_alue_id, tasokuvaukset_ORDER)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaukset_join_AUD (
    REV int4 not null,
    osaamiskokonaisuus_osa_alue_id int8 not null,
    tasokuvaukset_id int8 not null,
    tasokuvaukset_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaamiskokonaisuus_osa_alue_id, tasokuvaukset_id, tasokuvaukset_ORDER)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    taso varchar(255) not null,
    primary key (id)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    taso varchar(255),
    primary key (id, REV)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus_edistynytkuvaus_join (
    osaamiskokonaisuus_osa_alue_tasokuvaus_id int8 not null,
    edistynytOsaaminenKuvaukset_id int8 not null,
    edistynytOsaaminenKuvaukset_ORDER int4 not null,
    primary key (osaamiskokonaisuus_osa_alue_tasokuvaus_id, edistynytOsaaminenKuvaukset_ORDER)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus_edistynytkuvaus_join_AUD (
    REV int4 not null,
    osaamiskokonaisuus_osa_alue_tasokuvaus_id int8 not null,
    edistynytOsaaminenKuvaukset_id int8 not null,
    edistynytOsaaminenKuvaukset_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaamiskokonaisuus_osa_alue_tasokuvaus_id, edistynytOsaaminenKuvaukset_id, edistynytOsaaminenKuvaukset_ORDER)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus_kuvaus_join (
    osaamiskokonaisuus_osa_alue_tasokuvaus_id int8 not null,
    kuvaukset_id int8 not null,
    kuvaukset_ORDER int4 not null,
    primary key (osaamiskokonaisuus_osa_alue_tasokuvaus_id, kuvaukset_ORDER)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus_kuvaus_join_AUD (
    REV int4 not null,
    osaamiskokonaisuus_osa_alue_tasokuvaus_id int8 not null,
    kuvaukset_id int8 not null,
    kuvaukset_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaamiskokonaisuus_osa_alue_tasokuvaus_id, kuvaukset_id, kuvaukset_ORDER)
);

create table osaamiskokonaisuus_paa_alue (
    muokattu timestamp,
    id int8 not null,
    kuvaus_id int8,
    primary key (id)
);

create table osaamiskokonaisuus_paa_alue_AUD (
    id int8 not null,
    REV int4 not null,
    muokattu timestamp,
    kuvaus_id int8,
    primary key (id, REV)
);

create table osaamiskokonaisuus_paa_alue_osa_alueet_join (
    osaamiskokonaisuus_paa_alue_id int8 not null,
    osaAlueet_id int8 not null,
    osaAlueet_ORDER int4 not null,
    primary key (osaamiskokonaisuus_paa_alue_id, osaAlueet_ORDER)
);

create table osaamiskokonaisuus_paa_alue_osa_alueet_join_AUD (
    REV int4 not null,
    osaamiskokonaisuus_paa_alue_id int8 not null,
    osaAlueet_id int8 not null,
    osaAlueet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaamiskokonaisuus_paa_alue_id, osaAlueet_id, osaAlueet_ORDER)
);

alter table osaamiskokonaisuus_kasitteisto_join
    add constraint UK_c44o1n2fhxmyl6tug9ane49fk  unique (kasitteistot_id);

alter table osaamiskokonaisuus_osa_alue_tasokuvaukset_join
    add constraint UK_fkpmt296rdqt2cs9uditai0hm  unique (tasokuvaukset_id);

alter table osaamiskokonaisuus_paa_alue_osa_alueet_join
    add constraint UK_iklpraairy7tat6sm0tdxenfl  unique (osaAlueet_id);

alter table osaamiskokonaisuus
    add constraint FK_qk9kpp0cwh6yoosgrjvuapoir
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table osaamiskokonaisuus
    add constraint FK_4id0waqv745qxspm6xfmpmp09
    foreign key (id)
    references perusteenosa;

alter table osaamiskokonaisuus_AUD
    add constraint FK_kwmvywxx3olq7an8tud2el7g6
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table osaamiskokonaisuus_kasitteisto
    add constraint FK_elqwct8hi1uvwt0rj00r692y5
    foreign key (keskeinenKasitteisto_id)
    references tekstipalanen;

alter table osaamiskokonaisuus_kasitteisto
    add constraint FK_1bspopqe7xqp1pond3kyw9k0r
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table osaamiskokonaisuus_kasitteisto_AUD
    add constraint FK_5th4k81f40niicg1lc4ok3twm
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_kasitteisto_AUD
    add constraint FK_ihsug5xkcoq3r90cruy5aypwy
    foreign key (REVEND)
    references revinfo;

alter table osaamiskokonaisuus_kasitteisto_join
    add constraint FK_c44o1n2fhxmyl6tug9ane49fk
    foreign key (kasitteistot_id)
    references osaamiskokonaisuus_kasitteisto;

alter table osaamiskokonaisuus_kasitteisto_join
    add constraint FK_8jymd45repcbqnppblc4cmafm
    foreign key (osaamiskokonaisuus_id)
    references osaamiskokonaisuus;

alter table osaamiskokonaisuus_kasitteisto_join_AUD
    add constraint FK_4qa2vrrtlyktqpa41q4smp2l4
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_kasitteisto_join_AUD
    add constraint FK_pbxeivvur8q5pbbvj02nitpmi
    foreign key (REVEND)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue
    add constraint FK_m8yqx7vvd0ke5p9v6ancq7uee
    foreign key (nimi_id)
    references tekstipalanen;

alter table osaamiskokonaisuus_osa_alue_AUD
    add constraint FK_pbu1xp2mmxgv5j35n5fh9t3eb
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_AUD
    add constraint FK_2vgqedl49ho5hwatp0so6xoji
    foreign key (REVEND)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaukset_join
    add constraint FK_fkpmt296rdqt2cs9uditai0hm
    foreign key (tasokuvaukset_id)
    references osaamiskokonaisuus_osa_alue_tasokuvaus;

alter table osaamiskokonaisuus_osa_alue_tasokuvaukset_join
    add constraint FK_4la4iikdu3ojhgpwqi6i0hlmd
    foreign key (osaamiskokonaisuus_osa_alue_id)
    references osaamiskokonaisuus_osa_alue;

alter table osaamiskokonaisuus_osa_alue_tasokuvaukset_join_AUD
    add constraint FK_koac1vsweei5ss9hp4a0it2ob
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaukset_join_AUD
    add constraint FK_b87sem5awadyvegec6ve3rk6u
    foreign key (REVEND)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_AUD
    add constraint FK_j0834wi09jcvke6ory0ppshxl
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_AUD
    add constraint FK_si7whjdl9a9hl7jk1np76porc
    foreign key (REVEND)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_edistynytkuvaus_join
    add constraint FK_dvlgmfon6dblenvu4h0khiirw
    foreign key (edistynytOsaaminenKuvaukset_id)
    references tekstipalanen;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_edistynytkuvaus_join
    add constraint FK_6w7llf2qdxwf19is16kknb0oh
    foreign key (osaamiskokonaisuus_osa_alue_tasokuvaus_id)
    references osaamiskokonaisuus_osa_alue_tasokuvaus;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_edistynytkuvaus_join_AUD
    add constraint FK_5ydghwijbgg1rn7teoge3d6lr
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_edistynytkuvaus_join_AUD
    add constraint FK_l7hbkhpubkl08n9a1nex2caht
    foreign key (REVEND)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_kuvaus_join
    add constraint FK_a3552ryaa5v4ntyudtqpdafn6
    foreign key (kuvaukset_id)
    references tekstipalanen;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_kuvaus_join
    add constraint FK_fdsim7exl1rw459wlumodta8q
    foreign key (osaamiskokonaisuus_osa_alue_tasokuvaus_id)
    references osaamiskokonaisuus_osa_alue_tasokuvaus;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_kuvaus_join_AUD
    add constraint FK_r02s018fkvru8peu4brl438q7
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_kuvaus_join_AUD
    add constraint FK_210p2y25mkskpqexbsh3y1aqb
    foreign key (REVEND)
    references revinfo;

alter table osaamiskokonaisuus_paa_alue
    add constraint FK_5p7qw1yu4dimlhdjqmjhjblxj
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table osaamiskokonaisuus_paa_alue
    add constraint FK_scq34mihk7kncw65o865n6e1e
    foreign key (id)
    references perusteenosa;

alter table osaamiskokonaisuus_paa_alue_AUD
    add constraint FK_raxyr0gusq9dlr7oiwx3snvqj
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table osaamiskokonaisuus_paa_alue_osa_alueet_join
    add constraint FK_iklpraairy7tat6sm0tdxenfl
    foreign key (osaAlueet_id)
    references osaamiskokonaisuus_osa_alue;

alter table osaamiskokonaisuus_paa_alue_osa_alueet_join
    add constraint FK_mcuq64nn5aj451gtobmdhlo15
    foreign key (osaamiskokonaisuus_paa_alue_id)
    references osaamiskokonaisuus_paa_alue;

alter table osaamiskokonaisuus_paa_alue_osa_alueet_join_AUD
    add constraint FK_nabqqq30bryybdfbkr6x6cgso
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_paa_alue_osa_alueet_join_AUD
    add constraint FK_sd61yxt0jxnyjujtqf648xher
    foreign key (REVEND)
    references revinfo;