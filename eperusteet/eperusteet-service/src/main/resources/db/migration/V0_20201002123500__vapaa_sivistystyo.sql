drop view if exists perusteenosa_projekti;
drop table if exists opintokokonaisuus_arvioinnit;
drop table if exists opintokokonaisuus_arvioinnit_AUD;
drop table if exists opintokokonaisuus_opetuksen_tavoitteet;
drop table if exists opintokokonaisuus_opetuksen_tavoitteet_AUD;
drop table if exists opintokokonaisuus;
drop table if exists opintokokonaisuus_AUD;
drop table if exists vapaasivistystyo_perusteen_sisalto;
drop table if exists vapaasivistystyo_perusteen_sisalto_AUD;

create table opintokokonaisuus (
    laajuus int4,
    id int8 not null,
    kuvaus_id int8,
    nimiKoodi_id int8,
    opetuksenTavoiteOtsikko_id int8,
    primary key (id)
);

create table opintokokonaisuus_AUD (
    id int8 not null,
    REV int4 not null,
    laajuus int4,
    kuvaus_id int8,
    nimiKoodi_id int8,
    opetuksenTavoiteOtsikko_id int8,
    primary key (id, REV)
);

create table vapaasivistystyo_perusteen_sisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8 not null,
    sisalto_id int8,
    primary key (id)
);

create table vapaasivistystyo_perusteen_sisalto_AUD (
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

create table opintokokonaisuus_arvioinnit (
    peruste_id int8 not null,
    arviointi_id int8 not null,
    arvioinnit_ORDER int4 not null,
    primary key (peruste_id, arvioinnit_ORDER)
);

create table opintokokonaisuus_arvioinnit_AUD (
    REV int4 not null,
    peruste_id int8 not null,
    arviointi_id int8 not null,
    arvioinnit_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, peruste_id, arviointi_id, arvioinnit_ORDER)
);

create table opintokokonaisuus_opetuksen_tavoitteet (
    peruste_id int8 not null,
    opetuksentavoite_id int8 not null,
    opetuksenTavoitteet_ORDER int4 not null,
    primary key (peruste_id, opetuksenTavoitteet_ORDER)
);

create table opintokokonaisuus_opetuksen_tavoitteet_AUD (
    REV int4 not null,
    peruste_id int8 not null,
    opetuksentavoite_id int8 not null,
    opetuksenTavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, peruste_id, opetuksentavoite_id, opetuksenTavoitteet_ORDER)
);

alter table opintokokonaisuus_opetuksen_tavoitteet
    add constraint UK_2cgjsvdqsb3x4h5fquktkcrmq  unique (opetuksentavoite_id);

alter table opintokokonaisuus
    add constraint FK_oyhea3n8ydywvd87ahskkn3fu
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table opintokokonaisuus
    add constraint FK_9kydpr0sr9fmcetxqlf1k8bxw
    foreign key (nimiKoodi_id)
    references koodi;

alter table opintokokonaisuus
    add constraint FK_39u80kf7j11f643vvli7d2fp8
    foreign key (opetuksenTavoiteOtsikko_id)
    references tekstipalanen;

alter table opintokokonaisuus
    add constraint FK_mb6c4iqh8t3om7i4unvxnr4jg
    foreign key (id)
    references perusteenosa;

alter table opintokokonaisuus_AUD
    add constraint FK_iob3xt60dc0vyhb8mwfw12hnf
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table opintokokonaisuus_arvioinnit
    add constraint FK_2pjfurv52pkyrqemmgfu5pq8k
    foreign key (arviointi_id)
    references tekstipalanen;

alter table opintokokonaisuus_arvioinnit
    add constraint FK_onk2s6nkxaahpijw4r300wo96
    foreign key (peruste_id)
    references opintokokonaisuus;

alter table opintokokonaisuus_arvioinnit_AUD
    add constraint FK_iw6amsrjedq58mycm5rrek7bc
    foreign key (REV)
    references revinfo;

alter table opintokokonaisuus_arvioinnit_AUD
    add constraint FK_r3d5b1bg3f8utxhuotts63mkq
    foreign key (REVEND)
    references revinfo;

alter table opintokokonaisuus_opetuksen_tavoitteet
    add constraint FK_2cgjsvdqsb3x4h5fquktkcrmq
    foreign key (opetuksentavoite_id)
    references koodi;

alter table opintokokonaisuus_opetuksen_tavoitteet
    add constraint FK_3973bc8wikva933x9nrevu2fl
    foreign key (peruste_id)
    references opintokokonaisuus;

alter table opintokokonaisuus_opetuksen_tavoitteet_AUD
    add constraint FK_m3af8q8w8ee95ta0sdu5orlcs
    foreign key (REV)
    references revinfo;

alter table opintokokonaisuus_opetuksen_tavoitteet_AUD
    add constraint FK_4ocqnfd10ehctplejqpyvdns3
    foreign key (REVEND)
    references revinfo;

alter table vapaasivistystyo_perusteen_sisalto
    add constraint FK_jn3yn161jonh0f9yik5qtht5k
    foreign key (peruste_id)
    references peruste;

alter table vapaasivistystyo_perusteen_sisalto
    add constraint FK_s3ib2suj2ycscmpjo2jcc7k6e
    foreign key (sisalto_id)
    references perusteenosaviite;

alter table vapaasivistystyo_perusteen_sisalto_AUD
    add constraint FK_ngvxy01ajhpd1fwrmdsww5hv6
    foreign key (REV)
    references revinfo;

alter table vapaasivistystyo_perusteen_sisalto_AUD
    add constraint FK_794s1yp185m4bmj8cxbsww26
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

