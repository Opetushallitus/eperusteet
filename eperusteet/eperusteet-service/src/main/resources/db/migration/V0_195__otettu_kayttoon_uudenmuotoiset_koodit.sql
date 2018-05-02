--------------------------------------------------------------------------------
-- TutkinnonOsa
--------------------------------------------------------------------------------

-- Lisätään puuttuvat uudenmuotoiset koodit
INSERT INTO koodi (id, uri, koodisto, versio)
SELECT nextval('hibernate_sequence'),
       tosa.koodi_uri,
       'tutkinnonosat',
       NULL
FROM tutkinnonosa tosa
WHERE tosa.koodi_uri IS NOT NULL
  AND tosa.koodi_id IS NULL;

-- Päivitetään tutkinnon osiin uudenmuotoiset koodit
UPDATE tutkinnonosa
SET koodi_id =
  (SELECT id
   FROM koodi
   WHERE tutkinnonosa.koodi_id IS NULL
     AND tutkinnonosa.koodi_uri = koodi.uri
     AND koodi.koodisto = 'tutkinnonosat'
   LIMIT 1)
WHERE koodi_id IS NULL
  AND koodi_uri IS NOT NULL;

--------------------------------------------------------------------------------
-- OsaAlue
--------------------------------------------------------------------------------

-- Lisätään puuttuvat uudenmuotoiset koodit
INSERT INTO koodi (id, uri, koodisto, versio)
SELECT nextval('hibernate_sequence'),
       oa.koodi_uri,
       'tutkinnonosat',
       NULL
FROM tutkinnonosa_osaalue oa
WHERE oa.koodi_uri IS NOT NULL
  AND oa.koodi_id IS NULL;

-- Päivitetään osa alueille uudenmuotoiset koodit
UPDATE tutkinnonosa_osaalue
SET koodi_id =
  (SELECT id
   FROM koodi
   WHERE tutkinnonosa_osaalue.koodi_id IS NULL
     AND tutkinnonosa_osaalue.koodi_uri = koodi.uri
     AND koodi.koodisto = 'tutkinnonosat'
   LIMIT 1)
WHERE koodi_id IS NULL
  AND koodi_uri IS NOT NULL;

--------------------------------------------------------------------------------
-- Osaamistavoite
--------------------------------------------------------------------------------

-- Lisätään puuttuvat uudenmuotoiset koodit
INSERT INTO koodi (id, uri, koodisto, versio)
SELECT nextval('hibernate_sequence'),
       ot.koodi_uri,
       'ammatillisenoppiaineet',
       NULL
FROM osaamistavoite ot
WHERE ot.koodi_uri IS NOT NULL
  AND ot.koodi_id IS NULL;

-- Päivitetään osaamitavoitteille uudenmuotoiset koodit
UPDATE osaamistavoite
SET koodi_id =
  (SELECT id
   FROM koodi
   WHERE osaamistavoite.koodi_id IS NULL
     AND osaamistavoite.koodi_uri = koodi.uri
     AND koodi.koodisto = 'ammatillisenoppiaineet'
   LIMIT 1)
WHERE koodi_id IS NULL
  AND koodi_uri IS NOT NULL;