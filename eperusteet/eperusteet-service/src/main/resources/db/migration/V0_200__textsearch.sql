CREATE EXTENSION IF NOT EXISTS pg_trgm;

DROP TABLE IF EXISTS tekstihaku;
DROP TABLE IF EXISTS tekstihaku_tulos;
DROP TABLE IF EXISTS tekstihaku_links;
DROP TYPE IF EXISTS TekstihakuKind;


CREATE TYPE TekstihakuKind AS ENUM (
    'PERUSTE',
    'TEKSTIKAPPALE',
    'TUTKINNONOSA_OSAALUE',
    'TUTKINNONOSA_ARVIOINTI',
    'TUTKINNONOSA');


CREATE TABLE tekstihaku_links (
    tyyppi TekstihakuKind NOT NULL,
    tila VARCHAR(255),
    esikatseltavissa BOOLEAN,
    perusteprojekti_id BIGINT REFERENCES perusteprojekti(id) NOT NULL,
    peruste_id BIGINT REFERENCES peruste(id) NOT NULL,
    suoritustapa_id BIGINT,
    pov_id BIGINT,
    tov_id BIGINT,
    osaalue_id BIGINT,
    arviointi_id BIGINT
);


CREATE TABLE tekstihaku (
    id SERIAL NOT NULL,
    teksti_id BIGINT
) INHERITS (tekstihaku_links);


CREATE TABLE tekstihaku_tulos (
    id SERIAL NOT NULL,
    kieli VARCHAR(2),
    teksti TEXT
) INHERITS (tekstihaku_links);

-- Indeksien luonti
CREATE INDEX tekstihaku_trgm_idx ON tekstihaku_tulos USING GIN (teksti gin_trgm_ops);


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
            tekstipalanen tp,
            tutkinnonosa_tutkinnonosa_kevyttekstikappale ttk,
            kevyttekstikappale ktk
        WHERE pp.peruste_id = p.id
            AND p.id = pst.peruste_id AND pst.suoritustapa_id = st.id
            AND st.id = tov.suoritustapa_id
            AND tov.tutkinnonosa_id = tosa.id AND tov.tutkinnonosa_id = posa.id
            AND ttk.tutkinnonosa_id = tosa.id AND ttk.kevyttekstikappale_id = ktk.id
            AND (tp.id IN (posa.nimi_id,
                           tosa.kuvaus_id,
                           tosa.tavoitteet_id,
                           tosa.arviointi_id,
                           tosa.ammattitaitovaatimukset_id,
                           tosa.ammattitaidonosoittamistavat_id,
                           ktk.nimi_id,
                           ktk.teksti_id)); 
$$ LANGUAGE sql;


-- Tutkinnon osien osa-alueet
CREATE OR REPLACE FUNCTION haku_lisaa_tutkinnon_osien_osaalueet () RETURNS void AS $$
    INSERT
        INTO tekstihaku (tyyppi, perusteprojekti_id, peruste_id, suoritustapa_id, tov_id, osaalue_id, teksti_id)
        SELECT
            'TUTKINNONOSA_OSAALUE',
            pp.id AS perusteprojekti_id,
            p.id AS peruste_id,
            st.id AS suoritustapa_id,
            tov.id AS tov_id,
            osaalue.id AS osaalue_id,
            tp.id AS teksti_id
        FROM
            perusteprojekti pp,
            peruste p,
            peruste_suoritustapa pst,
            suoritustapa st,
            tutkinnonosaviite tov,
            tutkinnonosa tosa,
            tutkinnonosa_tutkinnonosa_osaalue tto,
            tutkinnonosa_osaalue osaalue,
            tutkinnonosa_osaalue_osaamistavoite oat,
            osaamistavoite tavoite,
            tekstipalanen tp
        WHERE pp.peruste_id = p.id
            AND p.id = pst.peruste_id AND pst.suoritustapa_id = st.id
            AND st.id = tov.suoritustapa_id
            AND tov.tutkinnonosa_id = tosa.id
            AND tosa.id = tto.tutkinnonosa_id AND tto.tutkinnonosa_osaalue_id = osaalue.id
            AND osaalue.id = oat.tutkinnonosa_osaalue_id AND oat.osaamistavoite_id = tavoite.id
            AND (tp.id IN (
                osaalue.nimi_id,
                osaalue.kuvaus_id,
                tavoite.nimi_id,
                tavoite.arviointi_id,
                tavoite.tavoitteet_id,
                tavoite.tunnustaminen_id,
                tavoite.esitieto_id
            )); 
$$ LANGUAGE sql;


-- Tutkinnon osien arviointi
CREATE OR REPLACE FUNCTION haku_lisaa_tutkinnon_arviointi () RETURNS void AS $$
    INSERT
        INTO tekstihaku (tyyppi, perusteprojekti_id, peruste_id, suoritustapa_id, tov_id, arviointi_id, teksti_id)
        SELECT
            'TUTKINNONOSA_ARVIOINTI',
            pp.id AS perusteprojekti_id,
            p.id AS peruste_id,
            st.id AS suoritustapa_id,
            tov.id AS tov_id,
            arviointi.id AS arviointi_id,
            tp.id AS teksti_id
        FROM
            perusteprojekti pp,
            peruste p,
            peruste_suoritustapa pst,
            suoritustapa st,
            tutkinnonosaviite tov,
            tutkinnonosa tosa,
            arviointi arviointi,
            arviointi_arvioinninkohdealue aak,
            arvioinninkohdealue arvioinninkohdealue,
            arvioinninkohdealue_arvioinninkohde,
            arvioinninkohde,
            arvioinninkohde_osaamistasonkriteeri akotk,
            osaamistasonkriteeri_tekstipalanen kriteeri,
            tekstipalanen tp
        WHERE pp.peruste_id = p.id
            AND p.id = pst.peruste_id AND pst.suoritustapa_id = st.id
            AND st.id = tov.suoritustapa_id
            AND tov.tutkinnonosa_id = tosa.id
            AND arviointi.id = tosa.arviointi_id
            AND arviointi.id = aak.arviointi_id AND aak.arvioinninkohdealue_id = arvioinninkohdealue.id
            AND arvioinninkohdealue_arvioinninkohde.arvioinninkohdealue_id = arvioinninkohdealue.id
                AND arvioinninkohde.id = arvioinninkohdealue_arvioinninkohde.arvioinninkohde_id
            AND akotk.arvioinninkohde_id = arvioinninkohde.id
                AND kriteeri.osaamistasonkriteeri_id = akotk.osaamistasonkriteerit_id
            AND (tp.id IN (
                arviointi.lisatiedot_id,
                arvioinninkohdealue.otsikko_id,
                arvioinninkohde.otsikko_id,
                arvioinninkohde.selite_id,
                kriteeri.tekstipalanen_id
            )); 
$$ LANGUAGE sql;


-- Tekstikappaleet
CREATE OR REPLACE FUNCTION add_tekstikappale_hierarkia(perusteprojekti bigint, peruste bigint, suoritustapa bigint, root_id bigint)
RETURNS VOID AS $$
    WITH RECURSIVE alitekstit AS (
        SELECT id, vanhempi_id, perusteenosa_id
        FROM perusteenosaviite
        WHERE vanhempi_id = root_id
        UNION SELECT
            pov.id,
            pov.vanhempi_id,
            pov.perusteenosa_id
        FROM perusteenosaviite pov
        INNER JOIN alitekstit vanhempi ON vanhempi.id = pov.vanhempi_id
    )
    INSERT INTO tekstihaku (tyyppi, perusteprojekti_id, peruste_id, suoritustapa_id, pov_id, teksti_id)
    SELECT
        'TEKSTIKAPPALE', perusteprojekti, peruste, suoritustapa, alt.id, tpt.tekstipalanen_id
    FROM alitekstit alt, perusteenosa osa, tekstikappale tk, tekstipalanen_teksti tpt
    WHERE
        alt.perusteenosa_id = osa.id AND osa.id = tk.id
        AND tpt.tekstipalanen_id IN (osa.nimi_id, tk.teksti_id);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION haku_lisaa_tekstikappaleet () RETURNS void AS $$
    SELECT
        add_tekstikappale_hierarkia(pp.id, p.id, s.id, s.sisalto_perusteenosaviite_id)
    FROM
        perusteprojekti pp,
        peruste p,
        peruste_suoritustapa ps,
        suoritustapa s
    WHERE
        pp.peruste_id = p.id
        AND p.id = ps.peruste_id AND ps.suoritustapa_id = s.id;
$$ LANGUAGE sql;


-- Hakuindeksin koostaminen
--------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION drop_old_tekstihaku() RETURNS VOID AS $$
BEGIN
    IF EXISTS(SELECT table_name FROM information_schema.tables WHERE table_name = 'tekstihaku_tulos')
    THEN
        DELETE FROM tekstihaku;
        DELETE FROM tekstihaku_tulos;
    END IF;
END
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION haku_pre () RETURNS void AS $$
    SELECT drop_old_tekstihaku();
$$ LANGUAGE sql;


CREATE OR REPLACE FUNCTION haku_post () RETURNS void AS $$
    INSERT INTO
        tekstihaku_tulos (tyyppi, perusteprojekti_id, peruste_id, suoritustapa_id, pov_id, tov_id, osaalue_id, arviointi_id, teksti)
    SELECT
        tyyppi,
        perusteprojekti_id,
        peruste_id,
        suoritustapa_id,
        pov_id,
        tov_id,
        osaalue_id,
        arviointi_id,
        string_agg(poista_tagit(get_teksti(teksti_id)), '\n') AS teksti
    FROM
        tekstihaku
    GROUP BY
        tyyppi,
        perusteprojekti_id,
        peruste_id,
        suoritustapa_id,
        pov_id,
        tov_id,
        osaalue_id,
        arviointi_id;

    -- Optimointeja tekstihakutaululle
    ALTER TABLE tekstihaku_tulos SET UNLOGGED;
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


-- NOTE: To create the text search index table, run:
SELECT rakenna_haku();
SELECT teksti FROM tekstihaku_tulos;

