drop view if exists perusteenosa_projekti;
drop table if exists koulutuksenosa_arvioinnit;
drop table if exists koulutuksenosa_arvioinnit_AUD;
drop table if exists koulutuksenosa_tavoitteet;
drop table if exists koulutuksenosa_tavoitteet_AUD;
drop table if exists koulutuksenosa;
drop table if exists koulutuksenosa_AUD;
drop table if exists tutkintoonvalmentava_perusteen_sisalto;
drop table if exists tutkintoonvalmentava_perusteen_sisalto_AUD;

create table tutkintoonvalmentava_perusteen_sisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8 not null,
    sisalto_id int8,
    primary key (id)
);

create table tutkintoonvalmentava_perusteen_sisalto_AUD (
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

alter table tutkintoonvalmentava_perusteen_sisalto
    add constraint FK_jynq8rx6hy2bb5g3nqwux32q1
    foreign key (peruste_id)
    references peruste;

alter table tutkintoonvalmentava_perusteen_sisalto
    add constraint FK_eg0t5mj38rxy35tinvwg3nfey
    foreign key (sisalto_id)
    references perusteenosaviite;

alter table tutkintoonvalmentava_perusteen_sisalto_AUD
    add constraint FK_myt0nicpg2u9d0sl6ewfjxo68
    foreign key (REV)
    references revinfo;

alter table tutkintoonvalmentava_perusteen_sisalto_AUD
    add constraint FK_rgm2o7uw33xxq4u7pk3dx3all
    foreign key (REVEND)
    references revinfo;

create table koulutuksenosa (
    koulutusOsanKoulutustyyppi varchar(255),
    laajuusMaksimi int4,
    laajuusMinimi int4,
    id int8 not null,
    keskeinenSisalto_id int8,
    kuvaus_id int8,
    nimiKoodi_id int8,
    osaamisenArvioinnista_id int8,
    primary key (id)
);

create table koulutuksenosa_AUD (
    id int8 not null,
    REV int4 not null,
    koulutusOsanKoulutustyyppi varchar(255),
    laajuusMaksimi int4,
    laajuusMinimi int4,
    keskeinenSisalto_id int8,
    kuvaus_id int8,
    nimiKoodi_id int8,
    osaamisenArvioinnista_id int8,
    primary key (id, REV)
);

create table koulutuksenosa_arvioinnit (
    koulutuksenosa_id int8 not null,
    arviointi_id int8 not null,
    arvioinnit_ORDER int4 not null,
    primary key (koulutuksenosa_id, arvioinnit_ORDER)
);

create table koulutuksenosa_arvioinnit_AUD (
    REV int4 not null,
    koulutuksenosa_id int8 not null,
    arviointi_id int8 not null,
    arvioinnit_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, koulutuksenosa_id, arviointi_id, arvioinnit_ORDER)
);

create table koulutuksenosa_tavoitteet (
    koulutuksenosa_id int8 not null,
    tavoite_id int8 not null,
    tavoitteet_ORDER int4 not null,
    primary key (koulutuksenosa_id, tavoitteet_ORDER)
);

create table koulutuksenosa_tavoitteet_AUD (
    REV int4 not null,
    koulutuksenosa_id int8 not null,
    tavoite_id int8 not null,
    tavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, koulutuksenosa_id, tavoite_id, tavoitteet_ORDER)
);

alter table koulutuksenosa
    add constraint FK_8gr6h21gjsfe7gf91itwyeta0
    foreign key (keskeinenSisalto_id)
    references tekstipalanen;

alter table koulutuksenosa
    add constraint FK_g8xkabegrpcoyaye2srp4dwfk
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table koulutuksenosa
    add constraint FK_b30mcsw4ox6h2no6w3vllpx1m
    foreign key (nimiKoodi_id)
    references koodi;

alter table koulutuksenosa
    add constraint FK_55dj3ne9pgwpp1qx8w1xj969q
    foreign key (osaamisenArvioinnista_id)
    references tekstipalanen;

alter table koulutuksenosa
    add constraint FK_lotfaqq8ct5fp6yigjuvuw2pe
    foreign key (id)
    references perusteenosa;

alter table koulutuksenosa_AUD
    add constraint FK_tk608pjsj3h8p4coup0qwlgqf
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table koulutuksenosa_arvioinnit
    add constraint FK_nfet78opktscwv2755grd52aw
    foreign key (arviointi_id)
    references tekstipalanen;

alter table koulutuksenosa_arvioinnit
    add constraint FK_rn4esbiw85vsnoh74l2ggi8be
    foreign key (koulutuksenosa_id)
    references koulutuksenosa;

alter table koulutuksenosa_arvioinnit_AUD
    add constraint FK_kosxjgxwrg3sjvg5o1s03fi82
    foreign key (REV)
    references revinfo;

alter table koulutuksenosa_arvioinnit_AUD
    add constraint FK_iytq4f7jvk7tiodocdu81fux6
    foreign key (REVEND)
    references revinfo;

alter table koulutuksenosa_tavoitteet
    add constraint FK_9a1vyacy4o2xn0wqk14ju5ayj
    foreign key (tavoite_id)
    references tekstipalanen;

alter table koulutuksenosa_tavoitteet
    add constraint FK_a5t1wwqoo78t27xe58yo8usor
    foreign key (koulutuksenosa_id)
    references koulutuksenosa;

alter table koulutuksenosa_tavoitteet_AUD
    add constraint FK_67u3s7mm7hdhwwcd5yyfhbl9a
    foreign key (REV)
    references revinfo;

alter table koulutuksenosa_tavoitteet_AUD
    add constraint FK_23xjff3ebq11n2pa6qill8d45
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
