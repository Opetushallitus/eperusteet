update tekstipalanen_teksti
set teksti = replace(replace(teksti, '<p>', ''), '</p>', '')
where tekstipalanen_id in (select tavoite_id from yl_opetuksen_tavoite)
  and teksti like '<p>%' and teksti like '%</p>'