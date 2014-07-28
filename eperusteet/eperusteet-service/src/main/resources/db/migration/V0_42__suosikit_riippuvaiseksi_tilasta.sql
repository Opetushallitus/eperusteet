DELETE FROM suosikki *;

ALTER TABLE suosikki DROP COLUMN suoritustapakoodi,
                     DROP COLUMN peruste_id,
                     ADD COLUMN parametrit text,
                     ADD COLUMN tila text,
                     ADD COLUMN nimi text,
                     ADD COLUMN lisatty timestamp;

ALTER TABLE suosikki ADD CONSTRAINT uk_suosikki_tila_parametrit UNIQUE(tila, parametrit);