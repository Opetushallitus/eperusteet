-- korjataan historia
insert into perusteenosaviite_aud
  select id,(select max(r.rev) from REVINFO r) as rev, 0 as revtype, null as revend, perusteenosa_id, vanhempi_id
  from perusteenosaviite v
  where not exists (select 0 from perusteenosaviite_aud a where a.id = v.id);

-- lisätään puuttuva otsikko (migraatio V0_66)
DO $$
    DECLARE seq int8;
BEGIN
    select nextval('hibernate_sequence') into seq;
    insert into tekstipalanen values(seq);
    insert into tekstipalanen_teksti values(seq,'FI','Tutkinnon muodostuminen');
    update perusteenosa set nimi_id = seq where tunniste = 'RAKENNE' and nimi_id is NULL;
    update perusteenosa_aud set nimi_id = seq where tunniste = 'RAKENNE' and nimi_id is NULL;
END$$;