CREATE TABLE yl_lukioopetuksen_perusteen_sisalto (
  id         BIGINT PRIMARY KEY             NOT NULL,
  peruste_id BIGINT REFERENCES peruste (id) NOT NULL UNIQUE,
  sisalto_id BIGINT REFERENCES perusteenosaviite (id),
  luoja      CHARACTER VARYING(255),
  luotu      TIMESTAMP WITHOUT TIME ZONE,
  muokattu   TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja  CHARACTER VARYING(255)
);

CREATE TABLE yl_lukioopetuksen_perusteen_sisalto_aud (
  id         BIGINT,
  peruste_id BIGINT  NOT NULL,
  sisalto_id BIGINT,
  luoja      CHARACTER VARYING(255),
  luotu      TIMESTAMP WITHOUT TIME ZONE,
  muokattu   TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja  CHARACTER VARYING(255),
  rev        INTEGER NOT NULL,
  revtype    SMALLINT,
  revend     INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);