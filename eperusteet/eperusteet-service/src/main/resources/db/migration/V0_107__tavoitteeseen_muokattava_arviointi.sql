ALTER TABLE yl_opetuksen_tavoite ADD COLUMN arvioinninotsikko_id bigint REFERENCES tekstipalanen(id);
ALTER TABLE yl_opetuksen_tavoite ADD COLUMN arvioinninkuvaus_id bigint REFERENCES tekstipalanen(id);
ALTER TABLE yl_opetuksen_tavoite ADD COLUMN arvioinninosaamisenKuvaus_id bigint REFERENCES tekstipalanen(id);

ALTER TABLE yl_opetuksen_tavoite_aud ADD COLUMN arvioinninotsikko_id bigint;
ALTER TABLE yl_opetuksen_tavoite_aud ADD COLUMN arvioinninkuvaus_id bigint;
ALTER TABLE yl_opetuksen_tavoite_aud ADD COLUMN arvioinninosaamisenKuvaus_id bigint;