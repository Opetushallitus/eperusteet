CREATE TABLE yl_lukio_opetussuunnitelma_rakenne (
  id         BIGINT PRIMARY KEY,
  sisalto_id BIGINT REFERENCES yl_lukiokoulutuksen_perusteen_sisalto (id) NOT NULL,
  viite_id   BIGINT REFERENCES perusteenosaviite (id)                     NOT NULL,
  luoja      CHARACTER VARYING(255),
  luotu      TIMESTAMP WITHOUT TIME ZONE DEFAULT now()                    NOT NULL,
  muokattu   TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja  CHARACTER VARYING(255)
);

CREATE TABLE yl_lukio_opetussuunnitelma_rakenne_aud (
  id         BIGINT,
  sisalto_id BIGINT,
  viite_id   BIGINT,
  luoja      CHARACTER VARYING(255),
  luotu      TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  muokattu   TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja  CHARACTER VARYING(255),
  rev        INTEGER NOT NULL,
  revtype    SMALLINT,
  revend     INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

-- perusteenosaviite_aud viite
ALTER TABLE perusteenosaviite_aud ADD COLUMN lapset_order INTEGER;

-- some helpers
CREATE OR REPLACE FUNCTION newId()
  RETURNS BIGINT AS $$
BEGIN
  RETURN (SELECT nextval('hibernate_sequence'))*10;
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insertAsRevision(tableName text, id BIGINT, _rev int)
  RETURNS INT AS $$
DECLARE
  cnames text[];
BEGIN
  SELECT array_agg(quote_ident(column_name) ORDER BY ordinal_position)
      INTO cnames FROM information_schema.columns
      WHERE table_name = tableName;
  EXECUTE 'INSERT INTO ' || quote_ident(tableName || '_aud')
          || '(' || array_to_string(cnames, ', ')
          || ', rev, revtype) SELECT v.*, $2 as rev, 0 as revtype FROM '
          || quote_ident(tableName) || ' v WHERE v.id = $1 AND NOT EXISTS( select a.rev from '
          || quote_ident(tableName || '_aud') || ' a where a.id = $1 and a.rev = $2 )'
    USING id, _rev;
  RETURN _rev;
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION newRevision()
  RETURNS INT AS $$
DECLARE
  _rev int;
BEGIN
  SELECT max(rev) INTO _rev FROM revinfo;
  _rev := _rev + 1;
  INSERT INTO revinfo(rev, revtstmp, kommentti) VALUES (_rev, extract(epoch from now()) * 1000, 'migraatio');
  RETURN _rev;
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insertAsRevision(tableName text, id BIGINT)
  RETURNS INT AS $$
BEGIN
  RETURN insertAsRevision(tableName, id, newRevision());
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION newTeksti(fi text)
  RETURNS BIGINT AS $$
DECLARE
  _id bigint;
BEGIN
  SELECT newId() INTO _id;
  INSERT INTO tekstipalanen(id) VALUES (_id);
  INSERT INTO tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) VALUES (_id, 'FI', fi);
  RETURN _id;
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION newOsa(_id bigint, _tunniste text, _nimi bigint, _tila text, _rev int)
  RETURNS BIGINT AS $$
BEGIN
  INSERT INTO perusteenosa(id, luotu, muokattu, nimi_id, luoja, muokkaaja, tila, tunniste)
    VALUES (_id, now(), null, _nimi, null, null, _tila, _tunniste);
  PERFORM insertAsRevision('perusteenosa', _id, _rev);
  RETURN _id;
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION newViite(vanhempiId bigint, perusteenosaId bigint)
  RETURNS BIGINT AS $$
DECLARE
  _id bigint;
BEGIN
  SELECT newId() INTO _id;
  INSERT INTO perusteenosaviite (id, vanhempi_id, lapset_order, perusteenosa_id) VALUES (_id, vanhempiId,
          coalesce((select max(v.lapset_order)+1 from perusteenosaviite v
                where v.vanhempi_id = vanhempiId), 0),
          perusteenosaId);
  PERFORM insertAsRevision('perusteenosaviite', _id);
  RETURN _id;
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION newViite(vanhempiId bigint)
  RETURNS BIGINT AS $$
BEGIN
  RETURN newViite(vanhempiId, null);
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION newOsa(_id bigint, _tunniste text, _nimi bigint, _tila text, viiteId bigint,
      _rev int)
  RETURNS BIGINT AS $$
BEGIN
  PERFORM newOsa(_id, _tunniste, _nimi, _tila, _rev);
  UPDATE perusteenosaviite SET perusteenosa_id = _id WHERE id = viiteId;
  RETURN _id;
END $$ LANGUAGE plpgsql;


-- create rakenne with viitteet to sisalto
INSERT INTO yl_lukio_opetussuunnitelma_rakenne (id, sisalto_id, viite_id)
  SELECT
    newId()                  AS id,
    sis.id                   AS sisalto_id,
    newViite(sis.sisalto_id) AS viite_id
  FROM yl_lukiokoulutuksen_perusteen_sisalto sis;


-- migrate kurssit to rakenne:
ALTER TABLE yl_lukiokurssi ADD COLUMN rakenne_id BIGINT REFERENCES yl_lukio_opetussuunnitelma_rakenne (id);
UPDATE yl_lukiokurssi
SET rakenne_id = (SELECT r.id
                  FROM yl_lukio_opetussuunnitelma_rakenne r
                  WHERE r.sisalto_id = yl_lukiokurssi.sisalto_id);
ALTER TABLE yl_lukiokurssi ALTER COLUMN rakenne_id SET NOT NULL;
ALTER TABLE yl_lukiokurssi DROP COLUMN sisalto_id;
ALTER TABLE yl_lukiokurssi_aud ADD COLUMN rakenne_id BIGINT;
ALTER TABLE yl_lukiokurssi_aud DROP COLUMN sisalto_id;

-- migrate oppiaineet to rakenne:
ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine ADD COLUMN rakenne_id BIGINT REFERENCES yl_lukio_opetussuunnitelma_rakenne (id);
UPDATE yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine
SET rakenne_id = (SELECT r.id
                  FROM yl_lukio_opetussuunnitelma_rakenne r
                  WHERE r.sisalto_id = yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine.sisalto_id);
ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine ALTER COLUMN rakenne_id SET NOT NULL;
ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine DROP COLUMN sisalto_id;
ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine_aud ADD COLUMN rakenne_id BIGINT;
ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine_aud DROP COLUMN sisalto_id;
ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine RENAME TO yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine;
ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto_yl_oppiaine_aud RENAME TO yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine_aud;


-- migrate new viite column to aihekokonaisuudet with parent in sisalto
ALTER TABLE yl_aihekokonaisuudet ADD COLUMN viite_id BIGINT REFERENCES perusteenosaviite (id);
UPDATE yl_aihekokonaisuudet
SET viite_id = newViite((SELECT s.sisalto_id
                         FROM yl_lukiokoulutuksen_perusteen_sisalto s
                         WHERE s.id = yl_aihekokonaisuudet.sisalto_id));
ALTER TABLE yl_aihekokonaisuudet ALTER COLUMN viite_id SET NOT NULL;
ALTER TABLE yl_aihekokonaisuudet_aud ADD COLUMN viite_id BIGINT;


SELECT ak.id, v.id, v.perusteenosa_id, vanhempi.id, vanhempi.perusteenosa_id FROM yl_lukio_opetussuunnitelma_rakenne ak
  INNER JOIN perusteenosaviite v ON ak.viite_id = v.id
  INNER JOIN perusteenosaviite vanhempi ON v.vanhempi_id = vanhempi.id
  INNER JOIN perusteenosa p ON p.id = ak.id;


-- migrate new viite column to yleiset tavoitteet with parent in sisalto
ALTER TABLE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet ADD COLUMN viite_id BIGINT REFERENCES perusteenosaviite (id);
UPDATE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet
SET viite_id = newViite((SELECT s.sisalto_id
                         FROM yl_lukiokoulutuksen_perusteen_sisalto s
                         WHERE s.id = yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet.sisalto_id));
ALTER TABLE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet ALTER COLUMN viite_id SET NOT NULL;
ALTER TABLE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet_aud ADD COLUMN viite_id BIGINT;



-- create missing osat:

-- opetuussuunnitelma:
SELECT insertAsRevision('yl_lukio_opetussuunnitelma_rakenne', r.id)
  FROM yl_lukio_opetussuunnitelma_rakenne r
  WHERE NOT EXISTS(SELECT a.rev FROM yl_lukio_opetussuunnitelma_rakenne_aud a WHERE a.id = r.id);
SELECT newOsa(rakenne.id, 'RAKENNE', newTeksti('Oppiaineet'), p.tila, rakenne.viite_id,
              (select min(a.rev) from yl_lukio_opetussuunnitelma_rakenne_aud a where a.id = rakenne.id))
  FROM yl_lukio_opetussuunnitelma_rakenne rakenne
    INNER JOIN "yl_lukiokoulutuksen_perusteen_sisalto" sisalto ON rakenne.sisalto_id = sisalto.id
    INNER JOIN peruste p ON p.id = sisalto.peruste_id;
ALTER TABLE yl_lukio_opetussuunnitelma_rakenne ADD FOREIGN KEY (id) REFERENCES perusteenosa(id);



-- aihekokonaisuudet:
-- if some references are null
update yl_aihekokonaisuudet
  set sisalto_id = (select max(id) from yl_lukiokoulutuksen_perusteen_sisalto ps
      where ps.id = yl_aihekokonaisuudet.id) where sisalto_id is null;
ALTER TABLE yl_aihekokonaisuudet ALTER COLUMN sisalto_id set NOT NULL;


SELECT newOsa(ak.id, 'NORMAALI', newTeksti('Aihekokonaisuudet'), p.tila, ak.viite_id,
          (select min(a.rev) from yl_aihekokonaisuudet_aud a where a.id = ak.id))
  FROM yl_aihekokonaisuudet ak
    INNER JOIN yl_lukiokoulutuksen_perusteen_sisalto sisalto ON ak.sisalto_id = sisalto.id
    INNER JOIN peruste p ON p.id = sisalto.peruste_id;
ALTER TABLE yl_aihekokonaisuudet ADD FOREIGN KEY (id) REFERENCES perusteenosa(id);

-- yleiset tavoitteet
SELECT newOsa(yt.id, 'NORMAALI', newTeksti('Opetuksen yleiset tavoitteet'), p.tila, yt.viite_id,
          (select min(a.rev) from yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet_aud a where a.id = yt.id))
FROM yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet yt
  INNER JOIN yl_lukiokoulutuksen_perusteen_sisalto sisalto ON yt.sisalto_id = sisalto.id
  INNER JOIN peruste p ON p.id = sisalto.peruste_id;
ALTER TABLE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet ADD FOREIGN KEY (id) REFERENCES perusteenosa(id);

-- Alun perin lapset_order jäi nollaksi, nyt ei pitäisi mutta tällä voi korjata paikalliseen kantaan:
-- CREATE OR REPLACE FUNCTION rearrangeViiteLapset(viiteiId bigint)
--   RETURNS INT AS $$
-- DECLARE
--   _maxJarjestys int := 0;
--   lapsi perusteenosaviite%ROWTYPE;
-- BEGIN
--   FOR lapsi IN SELECT v.* FROM perusteenosaviite v WHERE v.vanhempi_id = viiteiId
--           ORDER BY v.lapset_order, v.id LOOP
--     IF _maxJarjestys != lapsi.lapset_order THEN
--       UPDATE perusteenosaviite SET lapset_order = _maxJarjestys
--         WHERE perusteenosaviite.id = lapsi.id;
--     END IF;
--     _maxJarjestys := _maxJarjestys+1;
--   END LOOP;
--   RETURN _maxJarjestys;
-- END $$ LANGUAGE plpgsql;
--
-- SELECT rearrangeViiteLapset(v.id) FROM perusteenosaviite v;
