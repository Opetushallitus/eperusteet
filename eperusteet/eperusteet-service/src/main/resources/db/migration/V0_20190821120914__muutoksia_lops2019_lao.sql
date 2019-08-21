-- Siirretään osaamisen kohde laaja-alaisen osaamisen kuvaukseksi
UPDATE yl_lops2019_laaja_alainen_osaaminen lao
SET kuvaus_id = sub.kohde_id
FROM (
    SELECT laaja_alainen_osaaminen_id, kohde_id
    FROM yl_lops2019_laaja_alainen_osaaminen_tavoite
    INNER JOIN yl_lops2019_tavoite t
    ON yl_lops2019_laaja_alainen_osaaminen_tavoite.tavoite_id = t.id
) AS sub
WHERE lao.id = sub.laaja_alainen_osaaminen_id;

-- Poistetaan ylimääräiset taulut

-- lao tavoitteet
DROP TABLE yl_lops2019_laaja_alainen_osaaminen_tavoite;
DROP TABLE yl_lops2019_laaja_alainen_osaaminen_tavoite_AUD;
DROP TABLE yl_lops2019_tavoite;
DROP TABLE yl_lops2019_tavoite_AUD;
DROP TABLE yl_lops2019_tavoite_tavoite;
DROP TABLE yl_lops2019_tavoite_tavoite_AUD;
DROP TABLE yl_lops2019_tavoite_tavoite_tavoite;
DROP TABLE yl_lops2019_tavoite_tavoite_tavoite_AUD;

-- lao painopisteet
DROP TABLE yl_lops2019_laaja_alainen_osaaminen_painopiste;
DROP TABLE yl_lops2019_laaja_alainen_osaaminen_painopiste_AUD;
DROP TABLE yl_lops2019_oppiaine_painopiste;
DROP TABLE yl_lops2019_oppiaine_painopiste_AUD;

-- Lisätään lao koodi
ALTER TABLE yl_lops2019_laaja_alainen_osaaminen ADD COLUMN koodi_id int8;
ALTER TABLE yl_lops2019_laaja_alainen_osaaminen_AUD ADD COLUMN koodi_id int8;
