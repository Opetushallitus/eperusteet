alter table yl_lops2019_lao_kokonaisuus_lao add column jarjestys int4;
alter table yl_lops2019_lao_kokonaisuus_lao_aud add column jarjestys int4;

update yl_lops2019_lao_kokonaisuus_lao
set jarjestys = subquery.jarjestys
from (select laaja_alainen_osaaminen_kokonaisuus_id,
             laaja_alainen_osaaminen_id,
             row_number() over (partition by laaja_alainen_osaaminen_kokonaisuus_id order by laaja_alainen_osaaminen_id) - 1 as jarjestys
      from yl_lops2019_lao_kokonaisuus_lao) as subquery
where yl_lops2019_lao_kokonaisuus_lao.laaja_alainen_osaaminen_id = subquery.laaja_alainen_osaaminen_id
  and yl_lops2019_lao_kokonaisuus_lao.laaja_alainen_osaaminen_kokonaisuus_id =
      subquery.laaja_alainen_osaaminen_kokonaisuus_id;

update yl_lops2019_lao_kokonaisuus_lao_aud
set jarjestys = subquery.jarjestys
from (select rev, laaja_alainen_osaaminen_kokonaisuus_id,
             laaja_alainen_osaaminen_id,
             row_number() over (partition by rev, laaja_alainen_osaaminen_kokonaisuus_id order by laaja_alainen_osaaminen_id) -1 as jarjestys
      from yl_lops2019_lao_kokonaisuus_lao_aud) as subquery
where yl_lops2019_lao_kokonaisuus_lao_aud.rev = subquery.rev
  and yl_lops2019_lao_kokonaisuus_lao_aud.laaja_alainen_osaaminen_id = subquery.laaja_alainen_osaaminen_id
  and yl_lops2019_lao_kokonaisuus_lao_aud.laaja_alainen_osaaminen_kokonaisuus_id = subquery.laaja_alainen_osaaminen_kokonaisuus_id;

ALTER TABLE yl_lops2019_lao_kokonaisuus_lao ADD PRIMARY KEY (laaja_alainen_osaaminen_kokonaisuus_id, laaja_alainen_osaaminen_id, jarjestys);
alter table yl_lops2019_lao_kokonaisuus_lao_aud drop constraint IF EXISTS yl_lops2019_lao_kokonaisuus_lao_aud_pkey;
ALTER TABLE yl_lops2019_lao_kokonaisuus_lao_aud ADD PRIMARY KEY (REV, laaja_alainen_osaaminen_kokonaisuus_id, laaja_alainen_osaaminen_id, jarjestys);

ALTER TABLE yl_lops2019_lao_kokonaisuus_lao ALTER COLUMN jarjestys SET NOT NULL;
ALTER TABLE yl_lops2019_lao_kokonaisuus_lao_aud ALTER COLUMN jarjestys SET NOT NULL;