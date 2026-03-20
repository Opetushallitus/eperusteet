ALTER TABLE tekstikappale ADD COLUMN koodi_id int8;
ALTER TABLE tekstikappale_aud ADD COLUMN koodi_id int8;

ALTER TABLE tekstikappale
    ADD CONSTRAINT fk_tekstikappale_koodi
    FOREIGN KEY (koodi_id)
    REFERENCES koodi;

DROP TABLE IF EXISTS tekstikappale_koodi_aud CASCADE;

DROP TABLE IF EXISTS tekstikappale_koodi CASCADE;
