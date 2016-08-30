-- Lisätty lukiokursille arviointi valmentavaa lukiokoulutusta varten

ALTER TABLE yl_lukiokurssi ADD COLUMN arviointi_id BIGINT REFERENCES yl_tekstiosa(id);
ALTER TABLE yl_lukiokurssi_aud ADD COLUMN arviointi_id BIGINT;
