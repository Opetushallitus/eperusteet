CREATE TABLE termi (
  id int8 NOT NULL PRIMARY KEY,
  peruste_id int8 NOT NULL REFERENCES peruste(id),
  avain varchar NOT NULL,
  termi_id int8,
  selitys_id int8
);

CREATE TABLE termi_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  avain varchar,
  termi_id int8,
  selitys_id int8,
  primary key (id, REV)
);

ALTER TABLE termi ADD CONSTRAINT fk_termi_termi FOREIGN KEY (termi_id) REFERENCES tekstipalanen;
ALTER TABLE termi ADD CONSTRAINT fk_termi_selitys FOREIGN KEY (selitys_id) REFERENCES tekstipalanen;
ALTER TABLE termi ADD CONSTRAINT uk_uniikki_termi_perusteessa UNIQUE(peruste_id, avain);