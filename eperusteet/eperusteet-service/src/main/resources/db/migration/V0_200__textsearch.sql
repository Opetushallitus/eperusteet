CREATE EXTENSION IF NOT EXISTS pg_trgm;

DROP TABLE IF EXISTS tekstihaku;
DROP TABLE IF EXISTS tekstihaku_tulos;
DROP TABLE IF EXISTS tekstihaku_links;
DROP TYPE IF EXISTS TekstihakuKind;

CREATE TABLE tekstihaku_links (
    -- tyyppi VARCHAR(255) NOT NULL,
    tila VARCHAR(255),
    esikatseltavissa BOOLEAN,
    perusteprojekti_id BIGINT REFERENCES perusteprojekti(id),
    peruste_id BIGINT REFERENCES peruste(id),
    koulutustyyppi VARCHAR (255),
    suoritustapa_id BIGINT,
    pov_id BIGINT,
    tov_id BIGINT,
    tekstiKappale_id BIGINT,
    tutkinnonOsa_id BIGINT
);

CREATE TABLE tekstihaku (
    id SERIAL NOT NULL,
    kieli VARCHAR(2),
    kuvaus TEXT,
    teksti TEXT
) INHERITS (tekstihaku_links);

-- Indeksien luonti
CREATE INDEX tekstihaku_trgm_idx ON tekstihaku USING GIN (teksti gin_trgm_ops);


-- Rajoitetaan tekstihakurivien määrää toistaiseksi
-- CREATE OR REPLACE FUNCTION tekstihaku_cap_checker() RETURNS TRIGGER AS $$
-- BEGIN
--     IF (SELECT count(*) FROM tekstihaku_tulos) > 1000000 THEN
--         RAISE EXCEPTION 'Liikaa rivejä tekstihaulle';
--     END IF;
-- END
-- $$ LANGUAGE plpgsql;
--
-- CREATE TRIGGER tr_tekstihaku_cap_checker
--     BEFORE INSERT ON tekstihaku_tulos
--     FOR EACH ROW EXECUTE PROCEDURE tekstihaku_cap_checker();
