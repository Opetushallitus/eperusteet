-- Koodiston koodeille versio (käytetään vain jos halutaan ylikirjoittaa tarkkaan versioon)
ALTER TABLE koodi ADD COLUMN versio BIGINT;
ALTER TABLE koodi ADD COLUMN koodisto VARCHAR(255) NOT NULL;
ALTER TABLE koodi DROP COLUMN arvo;
ALTER TABLE koodi DROP COLUMN nimi_id;

-- Päivitetään tutkinnon rakenteen osaamisalat osoittamaan kooditauluun
ALTER TABLE tutkinnon_rakenne ADD COLUMN koodi_temp_id BIGINT REFERENCES koodi(id);
ALTER TABLE tutkinnon_rakenne_aud ADD COLUMN koodi_temp_id BIGINT REFERENCES koodi(id);

UPDATE tutkinnon_rakenne r
SET koodi_temp_id = k.id
FROM osaamisala o, koodi k
WHERE
    r.osaamisala_id = o.id
    AND k.uri = o.osaamisalakoodi_uri;

UPDATE tutkinnon_rakenne_aud r
SET koodi_temp_id = k.id
FROM osaamisala o, koodi k
WHERE
    r.osaamisala_id = o.id
    AND k.uri = o.osaamisalakoodi_uri;

-- Poistetaan vanha osaamisalaliitos
ALTER TABLE tutkinnon_rakenne DROP COLUMN osaamisala_id;
ALTER TABLE tutkinnon_rakenne_aud DROP COLUMN osaamisala_id;

ALTER TABLE tutkinnon_rakenne RENAME koodi_temp_id TO osaamisala_id;
ALTER TABLE tutkinnon_rakenne_aud RENAME koodi_temp_id TO osaamisala_id;