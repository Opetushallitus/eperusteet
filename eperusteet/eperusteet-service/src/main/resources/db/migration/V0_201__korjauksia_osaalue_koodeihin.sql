-- Siirretään pakollisen osaamistavoitteen koodi osa-alueen koodiksi jos sitä ei ole asetettu
WITH subquery AS
    (
        SELECT
            tutkinnonosa_osaalue.id as sub_id,
            osaamistavoite.koodi_id as sub_koodi_id,
            osaamistavoite.koodi_uri as sub_koodi_uri,
            osaamistavoite.koodi_arvo as sub_koodi_arvo
        FROM
            tutkinnonosa_osaalue
            INNER JOIN
                tutkinnonosa_osaalue_osaamistavoite
                ON tutkinnonosa_osaalue_osaamistavoite.tutkinnonosa_osaalue_id = tutkinnonosa_osaalue.id
            INNER JOIN
                osaamistavoite
                ON osaamistavoite.id = tutkinnonosa_osaalue_osaamistavoite.osaamistavoite_id
        WHERE
            tutkinnonosa_osaalue.koodi_id IS NULL
            AND pakollinen IS TRUE
            AND osaamistavoite.koodi_id IS NOT NULL
    )
UPDATE
    tutkinnonosa_osaalue
SET
    koodi_id = subquery.sub_koodi_id,
    koodi_uri = subquery.sub_koodi_uri,
    koodi_arvo = subquery.sub_koodi_arvo
FROM subquery
WHERE
    id = subquery.sub_id;