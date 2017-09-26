ALTER TABLE tutkinnonosa ADD COLUMN koodi_id BIGINT REFERENCES koodi(id);
ALTER TABLE tutkinnonosa_aud ADD COLUMN koodi_id BIGINT;
