ALTER TABLE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet_aud ALTER COLUMN otsikko_id DROP NOT NULL;
ALTER TABLE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet_aud ALTER COLUMN sisalto_id DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuudet_aud ALTER COLUMN otsikko_id DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuudet_aud ALTER COLUMN sisalto_id DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuudet_aud ALTER COLUMN luotu  DROP NOT NULL;

ALTER TABLE yl_aihekokonaisuudet_aud ALTER COLUMN luotu  DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuus_aud ALTER COLUMN otsikko_id DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuus_aud ALTER COLUMN yleiskuvaus_id DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuus_aud ALTER COLUMN luotu  DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuus_aud ALTER COLUMN aihekokonaisuudet_id  DROP NOT NULL;

