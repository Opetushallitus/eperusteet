ALTER TABLE yllapito DROP COLUMN IF EXISTS sallittu;
ALTER TABLE yllapito DROP COLUMN IF EXISTS url;
ALTER TABLE yllapito DROP COLUMN IF EXISTS ominaisuus;
ALTER TABLE yllapito_aud DROP COLUMN IF EXISTS sallittu;
ALTER TABLE yllapito_aud DROP COLUMN IF EXISTS url;
ALTER TABLE yllapito_aud DROP COLUMN IF EXISTS ominaisuus;

ALTER TABLE yllapito ADD COLUMN kuvaus VARCHAR(255) NOT NULL;
ALTER TABLE yllapito ADD COLUMN key VARCHAR(255) NOT NULL;
ALTER TABLE yllapito ADD COLUMN value VARCHAR(255) NOT NULL;
ALTER TABLE yllapito_aud ADD COLUMN kuvaus VARCHAR(255) NOT NULL;
ALTER TABLE yllapito_aud ADD COLUMN key VARCHAR(255) NOT NULL;
ALTER TABLE yllapito_aud ADD COLUMN value VARCHAR(255) NOT NULL;

INSERT INTO yllapito(id, kuvaus, key, value) VALUES (1, 'Eperusteet pdf-service käytössä', 'use-pdf-service-eperusteet', 'false');
INSERT INTO yllapito(id, kuvaus, key, value) VALUES (2, 'Amosaa pdf-service käytössä', 'use-pdf-service-amosaa', 'false');
INSERT INTO yllapito(id, kuvaus, key, value) VALUES (3, 'Ylops pdf-service käytössä', 'use-pdf-service-ylops', 'false');
