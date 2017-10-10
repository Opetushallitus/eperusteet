create table taiteenala (
    id int8 not null,
    teksti_id int8 references tekstipalanen(id),
    aikuistenOpetus_id int8 references kevyttekstikappale(id),
    kasvatus_id int8 references kevyttekstikappale(id),
    koodi_id int8 references kevyttekstikappale(id),
    oppimisenArviointiOpetuksessa_id int8 references kevyttekstikappale(id),
    teemaopinnot_id int8 references kevyttekstikappale(id),
    tyotavatOpetuksessa_id int8 references kevyttekstikappale(id),
    yhteisetOpinnot_id int8 references kevyttekstikappale(id),
    primary key (id)
);

create table taiteenala_AUD (
    id int8 not null,
    REV int4 not null,
    teksti_id int8,
    aikuistenOpetus_id int8,
    kasvatus_id int8,
    koodi_id int8,
    oppimisenArviointiOpetuksessa_id int8,
    teemaopinnot_id int8,
    tyotavatOpetuksessa_id int8,
    yhteisetOpinnot_id int8,
    primary key (id, REV)
);

create table tpo_perusteen_sisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8 not null references peruste(id),
    sisalto_id int8 references perusteenosaviite(id),
    primary key (id)
);

create table tpo_perusteen_sisalto_AUD (
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

CREATE OR REPLACE VIEW perusteenosa_projekti AS WITH RECURSIVE vanhemmat AS
  ( SELECT p.id,
           v.vanhempi_id,
           v.perusteenosa_id
   FROM perusteenosaviite v,
        perusteenosa p
   WHERE v.perusteenosa_id = p.id
   UNION ALL SELECT p.id,
                    p.vanhempi_id,
                    v.perusteenosa_id
   FROM perusteenosaviite p,
        vanhemmat v
   WHERE p.id = v.vanhempi_id)
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
