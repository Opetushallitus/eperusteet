UPDATE perusteen_muokkaustieto pm
SET kohde = 'perusopetusoppiaine'
FROM peruste p
WHERE p.id = pm.peruste_id
  AND pm.kohde = 'oppiaine'
  AND p.koulutustyyppi IN ('koulutustyyppi_16', 'koulutustyyppi_17');