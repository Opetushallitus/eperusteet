ALTER TABLE yl_aihekokonaisuudet ALTER COLUMN otsikko_id DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuudet ALTER COLUMN sisalto_id DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuudet_aud ALTER COLUMN otsikko_id DROP NOT NULL;
ALTER TABLE yl_aihekokonaisuudet_aud ALTER COLUMN sisalto_id  DROP NOT NULL;
