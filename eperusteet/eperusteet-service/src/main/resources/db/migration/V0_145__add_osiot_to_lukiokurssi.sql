-- lukion kursseihin osiot ja turhat kentät pois
ALTER TABLE yl_lukiokurssi DROP COLUMN kurssityypin_kuvaus_id;
ALTER TABLE yl_lukiokurssi DROP COLUMN  tavoitteet_id;
ALTER TABLE yl_lukiokurssi DROP COLUMN  sisallot_id;
ALTER TABLE yl_lukiokurssi_aud DROP COLUMN kurssityypin_kuvaus_id;
ALTER TABLE yl_lukiokurssi_aud DROP COLUMN  tavoitteet_id;
ALTER TABLE yl_lukiokurssi_aud DROP COLUMN  sisallot_id;

ALTER TABLE yl_lukiokurssi ADD COLUMN tavoitteet_id BIGINT REFERENCES yl_tekstiosa(id);
ALTER TABLE yl_lukiokurssi_aud ADD COLUMN tavoitteet_id BIGINT;

ALTER TABLE yl_lukiokurssi ADD COLUMN keskeinen_sisalto_id BIGINT REFERENCES yl_tekstiosa(id);
ALTER TABLE yl_lukiokurssi_aud ADD COLUMN keskeinen_sisalto_id BIGINT;

ALTER TABLE yl_lukiokurssi ADD COLUMN tavoitteet_ja_keskeinen_sisalto_id BIGINT REFERENCES yl_tekstiosa(id);
ALTER TABLE yl_lukiokurssi_aud ADD COLUMN tavoitteet_ja_keskeinen_sisalto_id BIGINT;