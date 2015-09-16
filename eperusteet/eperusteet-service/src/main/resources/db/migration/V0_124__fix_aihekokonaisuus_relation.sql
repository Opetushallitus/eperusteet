
DROP TABLE yl_oppiaine_yl_aihekokonaisuus_aud;
DROP TABLE yl_oppiaine_yl_aihekokonaisuus;
ALTER TABLE yl_aihekokonaisuus ADD COLUMN peruste_id INT8 REFERENCES peruste(id) NOT NULL DEFAULT NULL;
ALTER TABLE yl_aihekokonaisuus_aud ADD COLUMN peruste_id INT8 NOT NULL DEFAULT NULL;

