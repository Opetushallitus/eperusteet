UPDATE peruste p SET tila = 'VALMIS'
WHERE
tila = 'LUONNOS'
AND EXISTS (SELECT 1 FROM julkaistu_peruste j WHERE j.peruste_id = p.id);

UPDATE perusteprojekti p set tila = 'JULKAISTU'
WHERE
tila = 'LAADINTA'
AND EXISTS (SELECT 1 FROM julkaistu_peruste j WHERE j.peruste_id = p.peruste_id);

DROP INDEX IF EXISTS julkaistu_peruste_peruste_index;
DROP INDEX IF EXISTS tutkinnonosaviite_tutkinnonosa_index;

CREATE INDEX julkaistu_peruste_peruste_index ON julkaistu_peruste(peruste_id);
CREATE index tutkinnonosaviite_tutkinnonosa_index ON tutkinnonosaviite(tutkinnonosa_id);
