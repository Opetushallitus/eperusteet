ALTER TABLE tekstikappale ADD COLUMN osaamisala_id bigint REFERENCES koodi(id);
ALTER TABLE tekstikappale_aud ADD COLUMN osaamisala_id bigint;

