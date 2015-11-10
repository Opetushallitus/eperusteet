CREATE TABLE yl_kurssi (
  id         BIGINT PRIMARY KEY                        NOT NULL, -- hibernate_sequence used by application
  luoja      CHARACTER VARYING(255),
  luotu      TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu   TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja  CHARACTER VARYING(255),
  tunniste   UUID,
  nimi_id    BIGINT REFERENCES tekstipalanen (id)      NOT NULL,
  kuvaus_id  BIGINT REFERENCES tekstipalanen (id)      NOT NULL,
  koodi_uri  VARCHAR(255),
  koodi_arvo VARCHAR(255)
);
CREATE TABLE yl_kurssi_aud (
  id         BIGINT                                    NOT NULL,
  luoja      CHARACTER VARYING(255),
  luotu      TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu   TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja  CHARACTER VARYING(255),
  tunniste   UUID,
  nimi_id    BIGINT                                    NOT NULL,
  kuvaus_id  BIGINT                                    NOT NULL,
  koodi_uri  VARCHAR(255),
  koodi_arvo VARCHAR(255),
  rev        INTEGER                                   NOT NULL,
  revtype    SMALLINT,
  revend     INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);


CREATE TABLE yl_lukiokurssityyppi (
  tyyppi VARCHAR(64) PRIMARY KEY
);
INSERT INTO yl_lukiokurssityyppi (tyyppi)
VALUES ('PAKOLLINEN'),
  ('VALTAKUNNALLINEN_SYVENTAVA'),
  ('VALTAKUNNALLINEN_SOVELTAVA');


CREATE TABLE yl_lukiokurssi (
  id                     BIGINT PRIMARY KEY REFERENCES yl_kurssi (id)               NOT NULL,
  tyyppi                 VARCHAR(64) REFERENCES yl_lukiokurssityyppi (tyyppi)       NOT NULL,
  kurssityypin_kuvaus_id BIGINT REFERENCES tekstipalanen (id),
  tavoitteet_id          BIGINT REFERENCES tekstipalanen (id),
  sisallot_id            BIGINT REFERENCES tekstipalanen (id)
);
CREATE TABLE yl_lukiokurssi_aud (
  id                     BIGINT      NOT NULL,
  tyyppi                 VARCHAR(64) NOT NULL,
  kurssityypin_kuvaus_id BIGINT,
  tavoitteet_id          BIGINT REFERENCES tekstipalanen (id),
  sisallot_id            BIGINT REFERENCES tekstipalanen (id),
  rev                    INTEGER     NOT NULL,
  revtype                SMALLINT,
  revend                 INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

CREATE TABLE yl_oppaine_yl_lukiokurssi (
  id          BIGINT PRIMARY KEY                        NOT NULL, -- hibernate_sequence used by application
  luoja       CHARACTER VARYING(255),
  luotu       TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu    TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja   CHARACTER VARYING(255),
  oppiaine_id BIGINT REFERENCES yl_oppiaine (id)        NOT NULL,
  kurssi_id   BIGINT REFERENCES yl_lukiokurssi (id)     NOT NULL,
  jarjestys   INT
);
CREATE UNIQUE INDEX yl_oppaine_yl_lukiokurssi_single ON yl_oppaine_yl_lukiokurssi (oppiaine_id, kurssi_id);
CREATE UNIQUE INDEX yl_oppaine_yl_lukiokurssi_jarjestys ON yl_oppaine_yl_lukiokurssi (oppiaine_id, jarjestys)
  WHERE jarjestys IS NOT NULL;
CREATE TABLE yl_oppaine_yl_lukiokurssi_aud (
  id          BIGINT                                    NOT NULL,
  luoja       CHARACTER VARYING(255),
  luotu       TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu    TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja   CHARACTER VARYING(255),
  oppiaine_id BIGINT                                    NOT NULL,
  kurssi_id   BIGINT                                    NOT NULL,
  jarjestys   INT,
  rev         INTEGER                                   NOT NULL,
  revtype     SMALLINT,
  revend      INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);