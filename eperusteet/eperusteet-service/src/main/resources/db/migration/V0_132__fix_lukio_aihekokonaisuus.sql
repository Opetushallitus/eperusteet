CREATE TABLE yl_aihekokonaisuudet (
  id             BIGINT PRIMARY KEY                        NOT NULL, -- hibernate_sequence used by application
  luoja          CHARACTER VARYING(255),
  luotu          TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu       TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja      CHARACTER VARYING(255),
  tunniste       UUID,
  otsikko_id     BIGINT REFERENCES tekstipalanen(id) NOT NULL,
  yleiskuvaus_id BIGINT REFERENCES tekstipalanen(id),
  sisalto_id BIGINT REFERENCES yl_lukioopetuksen_perusteen_sisalto(id) NOT NULL
);

CREATE TABLE yl_aihekokonaisuudet_aud (
  id             BIGINT  NOT NULL,
  luoja          CHARACTER VARYING(255),
  luotu          TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu       TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja      CHARACTER VARYING(255),
  tunniste       UUID,
  otsikko_id     BIGINT NOT NULL,
  yleiskuvaus_id BIGINT,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  sisalto_id BIGINT NOT NULL,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

ALTER TABLE yl_aihekokonaisuus DROP COLUMN peruste_id;
ALTER TABLE yl_aihekokonaisuus_aud DROP COLUMN peruste_id;

ALTER TABLE yl_aihekokonaisuus ADD COLUMN jnro bigint DEFAULT 0;
ALTER TABLE yl_aihekokonaisuus_aud ADD COLUMN jnro bigint;
ALTER TABLE yl_aihekokonaisuus ADD COLUMN aihekokonaisuudet_id INT8 REFERENCES yl_aihekokonaisuudet(id) NOT NULL DEFAULT NULL;
ALTER TABLE yl_aihekokonaisuus_aud ADD COLUMN aihekokonaisuudet_id INT8 NOT NULL DEFAULT NULL;

ALTER TABLE yl_lukioopetuksen_perusteen_sisalto ADD COLUMN aihekokonaisuudet_id INT8 REFERENCES yl_aihekokonaisuudet(id) DEFAULT NULL;

ALTER TABLE yl_lukioopetuksen_perusteen_sisalto_aud ADD COLUMN aihekokonaisuudet_id INT8;
