UPDATE yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine_aud SET
  rakenne_id = (select actual.rakenne_id from yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine actual
        where actual.oppiaine_id = yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine_aud.oppiaine_id)
WHERE rakenne_id IS NULL AND revtype = 0
  AND EXISTS(SELECT * FROM yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine actual
    WHERE actual.oppiaine_id = yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine_aud.oppiaine_id);