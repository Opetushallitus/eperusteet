ALTER TABLE yl_aipe_vaihe ADD COLUMN jarjestys INTEGER;
ALTER TABLE yl_aipe_vaihe_aud ADD COLUMN jarjestys INTEGER;
ALTER TABLE yl_aipe_oppiaine ADD COLUMN jarjestys INTEGER;
ALTER TABLE yl_aipe_oppiaine_aud ADD COLUMN jarjestys INTEGER;
ALTER TABLE yl_aipe_kurssi ADD COLUMN jarjestys INTEGER;
ALTER TABLE yl_aipe_kurssi_aud ADD COLUMN jarjestys INTEGER;

UPDATE yl_aipe_vaihe SET jarjestys = 0 WHERE jarjestys IS NULL;
UPDATE yl_aipe_vaihe_aud SET jarjestys = 0 WHERE jarjestys IS NULL;
UPDATE yl_aipe_oppiaine SET jarjestys = 0 WHERE jarjestys IS NULL;
UPDATE yl_aipe_oppiaine_aud SET jarjestys = 0 WHERE jarjestys IS NULL;
UPDATE yl_aipe_kurssi SET jarjestys = 0 WHERE jarjestys IS NULL;
UPDATE yl_aipe_kurssi_aud SET jarjestys = 0 WHERE jarjestys IS NULL;

ALTER TABLE aipeoppiaine_aipeoppiaine DROP COLUMN oppimaara_order;
ALTER TABLE aipeoppiaine_aipeoppiaine_aud DROP COLUMN oppimaara_order;
ALTER TABLE aipevaihe_aipeoppiaine DROP COLUMN oppiaine_order;
ALTER TABLE aipevaihe_aipeoppiaine_aud DROP COLUMN oppiaine_order;
ALTER TABLE aipe_opetuksensisalto_vaihe DROP COLUMN vaihe_order;
ALTER TABLE aipe_opetuksensisalto_vaihe_aud DROP COLUMN vaihe_order;
ALTER TABLE aipeoppiaine_aipekurssi DROP COLUMN kurssit_order;
ALTER TABLE aipeoppiaine_aipekurssi_aud DROP COLUMN kurssit_order;