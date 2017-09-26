ALTER TABLE tutkinnonosa_osaalue ADD COLUMN koodi_id BIGINT REFERENCES koodi(id);
ALTER TABLE tutkinnonosa_osaalue_aud ADD COLUMN koodi_id BIGINT;
