ALTER TABLE yl_laajaalainen_osaaminen ADD COLUMN jarjestys INTEGER;
ALTER TABLE yl_laajaalainen_osaaminen_aud ADD COLUMN jarjestys INTEGER;

UPDATE yl_laajaalainen_osaaminen SET jarjestys = 0 WHERE jarjestys IS NULL;
UPDATE yl_laajaalainen_osaaminen_aud SET jarjestys = 0 WHERE jarjestys IS NULL;