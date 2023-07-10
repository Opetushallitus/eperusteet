ALTER TABLE peruste ADD COLUMN poikkeamismaarays_tyyppi CHARACTER VARYING(255);
ALTER TABLE peruste ADD COLUMN poikkeamismaarays_tarkennus BIGINT REFERENCES tekstipalanen(id);

ALTER TABLE peruste_aud ADD COLUMN poikkeamismaarays_tyyppi CHARACTER VARYING(255);
ALTER TABLE peruste_aud ADD COLUMN poikkeamismaarays_tarkennus BIGINT REFERENCES tekstipalanen(id);

update peruste set poikkeamismaarays_tyyppi = 'KOULUTUSVIENTILIITE'
               where id in (select distinct p.id from peruste p
                   INNER JOIN "peruste_liite" pl ON pl.peruste_id = p.id
                   INNER JOIN liite l ON l.id = pl.liite_id
                                                 where l.tyyppi = 'KOULUTUSVIENNINOHJE');
