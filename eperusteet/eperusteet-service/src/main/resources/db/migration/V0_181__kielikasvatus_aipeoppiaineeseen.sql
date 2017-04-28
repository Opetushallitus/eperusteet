ALTER TABLE yl_aipe_oppiaine ADD COLUMN kielikasvatus_id BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE yl_aipe_oppiaine_aud ADD COLUMN kielikasvatus_id BIGINT;