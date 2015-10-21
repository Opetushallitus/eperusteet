ALTER TABLE yl_oppiaine ADD COLUMN arviointi_id BIGINT REFERENCES yl_tekstiosa(id);
ALTER TABLE yl_oppiaine_aud ADD COLUMN arviointi_id BIGINT;
ALTER TABLE yl_oppiaine ADD COLUMN tavoitteet_id BIGINT REFERENCES yl_tekstiosa(id);
ALTER TABLE yl_oppiaine_aud ADD COLUMN tavoitteet_id BIGINT;
