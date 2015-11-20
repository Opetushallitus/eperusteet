ALTER TABLE tutkinnonosa_osaalue ADD COLUMN kuvaus_id bigint REFERENCES tekstipalanen(id);

ALTER TABLE tutkinnonosa_osaalue_aud ADD COLUMN kuvaus_id bigint REFERENCES tekstipalanen(id);
