ALTER TABLE arvioinninkohde ADD COLUMN selite_id bigint REFERENCES tekstipalanen(id);

ALTER TABLE arvioinninkohde_aud ADD COLUMN selite_id bigint REFERENCES tekstipalanen(id);