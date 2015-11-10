
CREATE TABLE yl_kurssi_toteuttava_oppiaine(
  kurssi_id INT8 REFERENCES yl_kurssi(id) NOT NULL,
  oppiaine_id INT8 REFERENCES yl_oppiaine(id) NOT NULL,
  PRIMARY KEY (kurssi_id, oppiaine_id)
);

CREATE TABLE yl_kurssi_toteuttava_oppiaine_aud (
  kurssi_id INT8 NOT NULL,
  oppiaine_id INT8 NOT NULL,
  rev        INTEGER                                   NOT NULL,
  revtype    SMALLINT,
  revend     INTEGER,
  PRIMARY KEY (rev, kurssi_id, oppiaine_id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

