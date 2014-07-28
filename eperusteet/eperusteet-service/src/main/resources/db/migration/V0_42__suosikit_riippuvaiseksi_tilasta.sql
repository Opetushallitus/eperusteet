DELETE FROM suosikki *;

ALTER TABLE suosikki DROP COLUMN suoritustapakoodi,
                     DROP COLUMN peruste_id,
                     ADD COLUMN parametrit text,
                     ADD COLUMN tila text,
                     ADD COLUMN nimi text,
                     ADD COLUMN lisatty timestamp,
                     ADD CONSTRAINT UNIQUE(tila, parametrit);