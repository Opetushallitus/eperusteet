CREATE TABLE kayttajaprofiili (
    id bigint NOT NULL PRIMARY KEY
);

CREATE TABLE kayttajaprofiili_peruste (
    kayttajaprofiili_id bigint REFERENCES kayttajaprofiili(id),
    peruste_id bigint REFERENCES peruste(id),
    suosikki_order integer
);

CREATE UNIQUE INDEX ix_suosikki 
  ON kayttajaprofiili_peruste(kayttajaprofiili_id, peruste_id);
