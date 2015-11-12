CREATE TABLE peruste_version(
  id SERIAL8 PRIMARY KEY,
  peruste_id INT8 REFERENCES peruste(id) NOT NULL UNIQUE,
  aikaleima TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);
INSERT INTO peruste_version(peruste_id) SELECT p.id as peruste_id FROM peruste p;
