-- selvittää perusteprojektin johon perusteenosa kuuluu
-- ottaa huomioon myös YL-perusteen
-- TODO: geneerisempi ratkaisu, tutkinnon osien irroittaminen tästä
create or replace view perusteenosa_projekti as
with recursive vanhemmat as (
    select p.id, v.vanhempi_id, v.perusteenosa_id from perusteenosaviite v, perusteenosa p
    where v.perusteenosa_id = p.id and p.tila='LUONNOS'
    union all
    select p.id, p.vanhempi_id, v.perusteenosa_id
    from perusteenosaviite p, vanhemmat v where p.id = v.vanhempi_id)
select distinct v.perusteenosa_id as id, pp.id as perusteprojekti_id, pp.ryhmaoid, pp.tila from
    vanhemmat v
    ,suoritustapa s
    ,peruste_suoritustapa ps
    ,peruste p
    ,perusteprojekti pp
where
    s.sisalto_perusteenosaviite_id = v.id
    and ps.suoritustapa_id = s.id
    and ps.peruste_id = p.id
    and pp.peruste_id = p.id
    and pp.tila not in ('JULKAISTU','POISTETTU')
union
select distinct v.perusteenosa_id as id, pp.id as perusteprojekti_id, pp.ryhmaoid, pp.tila from
    vanhemmat v
    ,yl_perusop_perusteen_sisalto s
    ,peruste p
    ,perusteprojekti pp
where
    s.sisalto_id = v.id
    and p.id = s.peruste_id
    and pp.peruste_id = p.id
    and pp.tila not in ('JULKAISTU','POISTETTU')
union
select distinct po.id, pp.id as perusteprojekti_id, pp.ryhmaoid, pp.tila from
     perusteenosa po
    ,tutkinnonosaviite v
    ,suoritustapa s
    ,peruste_suoritustapa ps
    ,peruste p
    ,perusteprojekti pp
where
    po.tila = 'LUONNOS'
    and po.id = v.tutkinnonosa_id
    and v.suoritustapa_id = s.id
    and ps.suoritustapa_id = s.id
    and ps.peruste_id = p.id
    and pp.peruste_id = p.id
    and pp.tila not in ('JULKAISTU','POISTETTU');
