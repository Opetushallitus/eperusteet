-- Lisätään oa lao liitos
ALTER TABLE yl_lops2019_oppiaine ADD COLUMN IF NOT EXISTS lao_id int8;
ALTER TABLE yl_lops2019_oppiaine_AUD ADD COLUMN IF NOT EXISTS lao_id int8;

-- Siirretään oa laot yhdeksi laoksi
UPDATE yl_lops2019_oppiaine oa
SET lao_id = sub.laaja_alainen_osaaminen_id
FROM (
    SELECT oppiaine_id, laaja_alainen_osaaminen_id
    FROM yl_lops2019_oppiaine_laaja_alaiset_osaamiset oa_lao
    INNER JOIN  yl_lops2019_oppiaine_laaja_alainen_osaaminen lao
    ON  oa_lao.laaja_alainen_osaaminen_id = lao.id
) AS sub
WHERE oa.id = sub.oppiaine_id;

-- Poisteaan ylimääräisiä tauluja
-- oa lao liitokset
DROP TABLE IF EXISTS yl_lops2019_oppiaine_laaja_alaiset_osaamiset;
DROP TABLE IF EXISTS yl_lops2019_oppiaine_laaja_alaiset_osaamiset_AUD;
-- oa lao järjetys
ALTER TABLE yl_lops2019_oppiaine_laaja_alainen_osaaminen DROP COLUMN IF EXISTS jarjestys;
ALTER TABLE yl_lops2019_oppiaine_laaja_alainen_osaaminen_AUD DROP COLUMN IF EXISTS jarjestys;
-- oa lao koodi
DROP TABLE IF EXISTS yl_lops2019_oppiaine_laaja_alainen_osaaminen_koodi;
DROP TABLE IF EXISTS yl_lops2019_oppiaine_laaja_alainen_osaaminen_koodi_AUD;