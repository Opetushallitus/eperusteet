
CREATE TABLE yl_lukioopetuksen_perusteen_sisalto_yl_oppiaine(
  sisalto_id BIGINT REFERENCES yl_lukioopetuksen_perusteen_sisalto(id) NOT NULL,
  oppiaine_id BIGINT REFERENCES yl_oppiaine(id) NOT NULL,
  PRIMARY KEY (sisalto_id, oppiaine_id)
);

CREATE TABLE yl_lukioopetuksen_perusteen_sisalto_yl_oppiaine_aud (
  sisalto_id BIGINT NOT NULL,
  oppiaine_id BIGINT NOT NULL,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  PRIMARY KEY (rev, sisalto_id, oppiaine_id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);


