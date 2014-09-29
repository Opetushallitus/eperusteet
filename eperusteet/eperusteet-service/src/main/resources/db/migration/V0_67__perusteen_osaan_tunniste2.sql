insert into tekstikappale(id) 
  select id from perusteenosa p where not exists (
    select 0 from tutkinnonosa where id = p.id union select 0 from tekstikappale where id = p.id
  );

insert into revinfo values (nextval('hibernate_sequence'), extract(EPOCH FROM now())*1000, 'migraatiomuutos', null);

insert into perusteenosa_aud 
  select id,(select max(r.rev) from REVINFO r) as rev, 0 as revtype, null as revend, luoja, luotu, muokattu, muokkaaja, nimi_id, tila, tunniste 
  from perusteenosa p 
  where not exists (
    select 0 from perusteenosa_aud a where a.id = p.id
  );

insert into tekstikappale_aud 
  select id,(select max(r.rev) from REVINFO r) as rev, teksti_id 
  from tekstikappale t 
  where not exists (select 0 from tekstikappale_aud a where a.id = t.id);

