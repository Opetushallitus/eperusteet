CREATE TABLE maarayskirje_liite (
  maarayskirje_id INT8 REFERENCES maarayskirje(id) NOT NULL,
  liite_id UUID REFERENCES liite(id) NOT NULL,
  liitteet_key INT4 NOT NULL,
  PRIMARY KEY (maarayskirje_id, liite_id, liitteet_key)
);

CREATE TABLE maarayskirje_liite_aud (
  maarayskirje_id INT8 REFERENCES maarayskirje(id) NOT NULL,
  liite_id UUID REFERENCES liite(id) NOT NULL,
  liitteet_key INT4 NOT NULL,
  rev INT4 NOT NULL,
  revtype INT2,
  revend INT4,
  PRIMARY KEY (rev, maarayskirje_id, liite_id, liitteet_key)
);
