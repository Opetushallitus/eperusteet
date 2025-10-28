drop table if exists kielijakaantajatutkinto_perusteen_sisalto cascade;
drop table if exists kielijakaantajatutkinto_perusteen_sisalto_aud cascade;

create table kielijakaantajatutkinto_perusteen_sisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8 not null,
    sisalto_id int8,
    primary key (id)
);

create table kielijakaantajatutkinto_perusteen_sisalto_aud (
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

alter table kielijakaantajatutkinto_perusteen_sisalto
    add constraint FK_1luja347osx8n614se9xck222
    foreign key (peruste_id)
    references peruste;

alter table kielijakaantajatutkinto_perusteen_sisalto
    add constraint FK_2wq4f86qg1iw5bhuvhc47h222
    foreign key (sisalto_id)
    references perusteenosaviite;

alter table kielijakaantajatutkinto_perusteen_sisalto_aud
    add constraint FK_2eppp8blqx6itfclp811p5222
    foreign key (REV)
    references revinfo;

alter table kielijakaantajatutkinto_perusteen_sisalto_aud
    add constraint FK_dcigftkt7rvyfq45np7d0g222
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
     kielijakaantajatutkinto_perusteen_sisalto s ,
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
