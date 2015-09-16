CREATE TABLE yl_aihekokonaisuus (
  id             BIGINT PRIMARY KEY                        NOT NULL, -- hibernate_sequence used by application
  luoja          CHARACTER VARYING(255),
  luotu          TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu       TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja      CHARACTER VARYING(255),
  tunniste       UUID,
  otsikko_id     BIGINT REFERENCES tekstipalanen(id) NOT NULL,
  yleiskuvaus_id BIGINT REFERENCES tekstipalanen(id)
);

CREATE TABLE yl_aihekokonaisuus_aud (
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
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

CREATE TABLE yl_oppiaine_yl_aihekokonaisuus (
  oppiaine_id BIGINT REFERENCES yl_oppiaine(id) NOT NULL,
  aihekokonaisuus_id BIGINT REFERENCES yl_aihekokonaisuus(id) NOT NULL,
  PRIMARY KEY (oppiaine_id, aihekokonaisuus_id)
);

CREATE TABLE yl_oppiaine_yl_aihekokonaisuus_aud (
  oppiaine_id BIGINT NOT NULL,
  aihekokonaisuus_id BIGINT NOT NULL,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  PRIMARY KEY (rev, oppiaine_id, aihekokonaisuus_id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

