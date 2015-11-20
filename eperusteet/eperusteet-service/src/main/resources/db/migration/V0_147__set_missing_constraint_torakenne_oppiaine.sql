-- remove duplicates
DELETE FROM yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine
WHERE ctid NOT IN (SELECT min(ctid) AS min_ctid
                   FROM yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine
                   GROUP BY oppiaine_id, rakenne_id);

ALTER TABLE yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine ADD PRIMARY KEY (oppiaine_id, rakenne_id);
