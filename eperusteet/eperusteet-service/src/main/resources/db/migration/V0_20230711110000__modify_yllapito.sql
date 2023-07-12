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
