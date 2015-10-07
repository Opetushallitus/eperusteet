
CREATE TABLE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet (
  id             BIGINT PRIMARY KEY                        NOT NULL, -- hibernate_sequence used by application
  luoja          CHARACTER VARYING(255),
  luotu          TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu       TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja      CHARACTER VARYING(255),
  tunniste       UUID,
  otsikko_id     BIGINT REFERENCES tekstipalanen(id) NOT NULL,
  kuvaus_id BIGINT REFERENCES tekstipalanen(id),
  sisalto_id BIGINT REFERENCES yl_lukiokoulutuksen_perusteen_sisalto(id) NOT NULL
);

CREATE TABLE yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet_aud (
  id             BIGINT  NOT NULL,
  luoja          CHARACTER VARYING(255),
  luotu          TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu       TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja      CHARACTER VARYING(255),
  tunniste       UUID,
  otsikko_id     BIGINT NOT NULL,
  kuvaus_id BIGINT,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  sisalto_id BIGINT NOT NULL,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto ADD COLUMN opetuksen_yleiset_tavoitteet_id INT8 REFERENCES yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet(id) DEFAULT NULL;
ALTER TABLE yl_lukiokoulutuksen_perusteen_sisalto_aud ADD COLUMN opetuksen_yleiset_tavoitteet_id INT8;