CREATE TABLE tekstihaku (
    perusteprojekti_id BIGINT REFERENCES perusteprojekti(id) NOT NULL,
    peruste_id BIGINT REFERENCES peruste(id) NOT NULL,
    suoritustapa_id BIGINT REFERENCES perusteprojekti(id),
    viite_id BIGINT REFERENCES perusteenosaviite(id),
    teksti TEXT
);

CREATE OR REPLACE FUNCTION poista_vanha_haku () RETURNS void AS $$
    DROP VIEW IF EXISTS tekstihaku_view;
    DROP MATERIALIZED VIEW IF EXISTS tekstihaku;
    DROP INDEX IF EXISTS index_tekstihaku_tekstikappale;
    DROP INDEX IF EXISTS trgm_idx;
    DROP extension IF EXISTS pg_trgm;
$$ LANGUAGE sql;


CREATE OR REPLACE FUNCTION poista_tagit(teksti TEXT)
    RETURNS TEXT
    AS $$
        SELECT regexp_replace(teksti, E'<[^>]+>', '', 'gi');
    $$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION collect_tekstikappaleet(root_id BIGINT)
    RETURNS TABLE (id BIGINT, vanhempi_id BIGINT, perusteenosa_id BIGINT)
    AS $$
        WITH RECURSIVE haku AS (
            SELECT id, vanhempi_id, perusteenosa_id
                FROM perusteenosaviite
                WHERE vanhempi_id = root_id
            UNION ALL
                SELECT pov.id, pov.vanhempi_id, pov.perusteenosa_id
                    FROM haku h, perusteenosaviite pov
                    WHERE pov.vanhempi_id = h.id
        )
        SELECT id, vanhempi_id, perusteenosa_id FROM haku;
    $$
    LANGUAGE 'sql'
    IMMUTABLE;


CREATE OR REPLACE FUNCTION concat_tekstikappaleet(root_id BIGINT)
    RETURNS TEXT
    AS $$
        SELECT poista_tagit(string_agg(teksti, E'\n'))
        FROM tekstikappale tk, collect_tekstikappaleet(root_id) h, tekstipalanen_teksti tpt
        WHERE h.perusteenosa_id = tk.id
            and tpt.tekstipalanen_id = tk.teksti_id;
    $$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION rakenna_tekstihaku ()
    RETURNS void
    AS $$
        SELECT poista_vanha_haku();
        CREATE EXTENSION pg_trgm;

        CREATE MATERIALIZED VIEW tekstihaku AS
            SELECT DISTINCT ON (tpt.tekstipalanen_id)
                pp.id AS perusteprojekti_id,
                p.id AS peruste_id,
                tekstikappale.id AS tekstikappale_id,
                s.id AS suoritustapa_id,
                pov.id AS viite_id,
                tp.id AS tekstipalanen_id,
                tpt.kieli AS kieli

--                CASE
--                    WHEN tekstikappale.id = pov.vanhempi_id THEN concat_tekstikappaleet(pov.id)
--                    ELSE NULL
--                END AS teksti,
--
--                CASE
--                    WHEN tekstikappale.id = pov.vanhempi_id THEN
--                        concat_tekstikappaleet(pov.id)
--                    ELSE
--                        poista_tagit(tpt.teksti)
--                END AS teksti

            FROM
                perusteprojekti pp,
                peruste p,
                peruste_suoritustapa ps,
                suoritustapa s,

                perusteenosaviite pov,
                perusteenosa perusteenosa,
                tekstikappale tekstikappale,
                tekstipalanen tp,
                tekstipalanen_teksti tpt

            WHERE pp.peruste_id = p.id
    --            AND pp.tila = 'JULKAISTU'
                AND tp.id = tpt.tekstipalanen_id
                AND (p.nimi_id = tp.id
                    OR p.kuvaus_id = tp.id
                    OR perusteenosa.nimi_id = tp.id
                    OR ((ps.peruste_id = p.id AND ps.suoritustapa_id = s.id AND s.sisalto_perusteenosaviite_id = pov.vanhempi_id) AND
                        tekstikappale.teksti_id = tp.id AND tekstikappale.id = perusteenosa.id));

        CREATE VIEW tekstihaku_view AS SELECT * FROM tekstihaku; -- NOTE: Hibernate validaattori ei ymmärrä materialisoituja näkymiä
    --    CREATE INDEX index_tekstihaku_tekstikappale ON tekstihaku USING GIN (to_tsvector('simple', teksti));
        CREATE INDEX trgm_idx ON tekstihaku USING GIN (teksti gin_trgm_ops);
    $$ LANGUAGE sql;
