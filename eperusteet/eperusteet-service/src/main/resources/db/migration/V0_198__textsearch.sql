CREATE EXTENSION IF NOT EXISTS pg_trgm;

DROP TABLE IF EXISTS tekstihaku;
DROP TABLE IF EXISTS tekstihaku_t;
DROP TYPE IF EXISTS TekstihakuKind;

CREATE TYPE TekstihakuKind AS ENUM (
    'PERUSTE',
    'TEKSTIKAPPALE',
    'TUTKINNONOSA');

CREATE TABLE tekstihaku (
    id SERIAL NOT NULL,
    tyyppi TekstihakuKind NOT NULL,
    perusteprojekti_id BIGINT REFERENCES perusteprojekti(id) NOT NULL,
    peruste_id BIGINT REFERENCES peruste(id) NOT NULL,
    suoritustapa_id BIGINT,
    pov_id BIGINT,
    tov_id BIGINT,
    teksti_id BIGINT
);


-- Yleiskäyttöinen koodi tekstiindeksin luomiseen
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION poista_tagit(teksti TEXT)
    -- Poistaa teksteistä tagit
    RETURNS TEXT
    AS $$
        SELECT regexp_replace(teksti, E'<[^>]+>', '', 'gi');
    $$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION get_teksti(teksti_id BIGINT, kieli varchar(2))
    -- Hakee teksti
    RETURNS TEXT
    AS $$
        SELECT tpt.teksti
            FROM tekstipalanen_teksti tpt
            WHERE tpt.tekstipalanen_id = teksti_id AND tpt.kieli = kieli;
    $$ LANGUAGE sql IMMUTABLE;


CREATE OR REPLACE FUNCTION get_teksti(teksti_id BIGINT)
    -- Hakee teksti
    RETURNS TEXT
    AS $$
        SELECT tpt.teksti
            FROM tekstipalanen_teksti tpt
            WHERE tpt.tekstipalanen_id = teksti_id;
    $$ LANGUAGE sql IMMUTABLE;


--------------------------------------------------------------------------------


-- Perusteet
CREATE OR REPLACE FUNCTION haku_lisaa_perusteet () RETURNS void AS $$
    INSERT INTO tekstihaku (tyyppi, perusteprojekti_id, peruste_id, teksti_id)
    SELECT
        'PERUSTE',
        pp.id AS perusteprojekti_id,
        p.id AS peruste_id,
        tp.id AS teksti_id
    FROM
        perusteprojekti pp,
        peruste p,
        tekstipalanen tp
    WHERE pp.peruste_id = p.id
        AND tp.id IN (
            p.nimi_id,
            p.kuvaus_id);
$$ LANGUAGE sql;

-- Tutkinnon osat
CREATE OR REPLACE FUNCTION haku_lisaa_tutkinnon_osat () RETURNS void AS $$
    INSERT
        INTO tekstihaku (tyyppi, perusteprojekti_id, peruste_id, suoritustapa_id, tov_id, teksti_id)
        SELECT
            'TUTKINNONOSA',
            pp.id AS perusteprojekti_id,
            p.id AS peruste_id,
            st.id AS suoritustapa_id,
            tov.id AS tov_id,
            tp.id AS teksti_id
        FROM
            perusteprojekti pp,
            peruste p,
            peruste_suoritustapa pst,
            suoritustapa st,
            tutkinnonosaviite tov,
            perusteenosa posa,
            tutkinnonosa tosa,
            tekstipalanen tp
        WHERE pp.peruste_id = p.id
            AND p.id = pst.peruste_id AND pst.suoritustapa_id = st.id
            AND st.id = tov.suoritustapa_id
            AND tov.tutkinnonosa_id = tosa.id AND tov.tutkinnonosa_id = posa.id
            AND tp.id IN (
                posa.nimi_id,
                tosa.kuvaus_id,
                tosa.tavoitteet_id,
                tosa.arviointi_id,
                tosa.ammattitaitovaatimukset_id,
                tosa.ammattitaidonosoittamistavat_id
            ); 
$$ LANGUAGE sql;


-- Tekstikappaleet
CREATE OR REPLACE FUNCTION haku_lisaa_tekstikappaleet () RETURNS void AS $$
    WITH RECURSIVE alitekstit AS (
        SELECT id, vanhempi_id, perusteenosa_id
        FROM perusteenosaviite
        WHERE vanhempi_id IS NULL
        UNION SELECT
            pov.id,
            pov.vanhempi_id,
            pov.perusteenosa_id
        FROM perusteenosaviite pov
        INNER JOIN alitekstit vanhempi ON vanhempi.id = pov.vanhempi_id
    )
    INSERT INTO tekstihaku (tyyppi, perusteprojekti_id, peruste_id, suoritustapa_id, pov_id, teksti_id)
        SELECT
            'TEKSTIKAPPALE' AS tyyppi,
            pp.id AS perusteprojekti_id,
            p.id AS peruste_id,
            s.id AS suoritustapa_id,
            alt.id AS pov_id,
            tp.id AS teksti_id
        FROM
            alitekstit alt,
            perusteprojekti pp,
            peruste p,
            peruste_suoritustapa ps,
            suoritustapa s,
            perusteenosa posa,
            tekstikappale tk,
            tekstipalanen tp
        WHERE pp.peruste_id = p.id
            AND p.id = ps.peruste_id AND ps.suoritustapa_id = s.id
            AND s.sisalto_perusteenosaviite_id = alt.vanhempi_id
            AND alt.perusteenosa_id = posa.id AND alt.perusteenosa_id = tk.id
            AND tp.id IN (
                posa.nimi_id,
                tk.teksti_id
            ); 

$$ LANGUAGE sql;


-- Hakuindeksin koostaminen
--------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION haku_pre () RETURNS void AS $$
    -- DELETE FROM tekstihaku;
    -- DELETE FROM tekstihaku_t;
$$ LANGUAGE sql;


CREATE OR REPLACE FUNCTION haku_post () RETURNS void AS $$
    SELECT tyyppi, perusteprojekti_id, peruste_id, suoritustapa_id, pov_id, tov_id,
        string_agg(poista_tagit(get_teksti(teksti_id)), '\n') AS teksti
        INTO tekstihaku_t
        FROM tekstihaku
        GROUP BY tyyppi, perusteprojekti_id, peruste_id, suoritustapa_id, pov_id, tov_id;

    -- Optimointeja tekstihakutaululle
    ALTER TABLE tekstihaku_t SET UNLOGGED;

    -- Indeksien luonti
    CREATE INDEX tekstihaku_trgm_idx ON tekstihaku_t USING GIN (teksti gin_trgm_ops);
$$ LANGUAGE sql;


-- NOTE: Ylikirjoitettava uusia kohteita lisätessä
CREATE OR REPLACE FUNCTION hakukohteet() RETURNS void AS $$ BEGIN
    PERFORM haku_lisaa_perusteet();
    PERFORM haku_lisaa_tekstikappaleet();
    PERFORM haku_lisaa_tutkinnon_osat();
END $$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION rakenna_haku() RETURNS void AS $$ BEGIN
    PERFORM haku_pre();
    PERFORM hakukohteet();
    PERFORM haku_post();
END $$ LANGUAGE plpgsql;


SELECT rakenna_haku();
