ALTER TABLE peruste ADD COLUMN julkisivu_jarjestys_nro INTEGER DEFAULT 0 NOT NULL;
ALTER TABLE peruste ADD COLUMN piilota_julkisivulta BOOLEAN DEFAULT FALSE;

ALTER TABLE peruste_aud ADD COLUMN julkisivu_jarjestys_nro INTEGER;
ALTER TABLE peruste_aud ADD COLUMN piilota_julkisivulta BOOLEAN;