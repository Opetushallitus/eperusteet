CREATE TABLE maarayskirje_liite (
  maarayskirje_id INT8 REFERENCES maarayskirje(id) NOT NULL,
  liite_id UUID REFERENCES liite(id) NOT NULL,
  PRIMARY KEY (maarayskirje_id, liite_id)
);
