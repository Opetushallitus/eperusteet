ALTER TABLE ONLY peruste
    DROP CONSTRAINT IF EXISTS fk_peruste_koulutusala;

ALTER TABLE ONLY peruste
    DROP IF EXISTS koulutusala_id;

DROP TABLE IF EXISTS koulutusala_opintoala;
DROP TABLE IF EXISTS peruste_opintoala;
DROP TABLE IF EXISTS opintoala;
DROP TABLE IF EXISTS koulutusala;

CREATE TABLE koulutus (
    id bigint NOT NULL PRIMARY KEY,
    koulutus_koodi character varying(255) NOT NULL UNIQUE,
    koulutusala_koodi character varying(255),
    opintoala_koodi character varying(255)
);

CREATE TABLE peruste_koulutus (
    peruste_id bigint REFERENCES peruste(id),
    koulutus_id bigint REFERENCES koulutus(id)
);