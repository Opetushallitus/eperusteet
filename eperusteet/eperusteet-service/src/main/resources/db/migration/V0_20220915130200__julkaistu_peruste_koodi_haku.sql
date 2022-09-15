drop trigger if exists tg_refresh_julkaistu_peruste_data_view on julkaistu_peruste;
drop trigger if exists tg_refresh_julkaistu_peruste_data_view on peruste;
drop function if exists tg_refresh_julkaistu_peruste_data_view;
drop materialized view if exists julkaistu_peruste_data_view;

create materialized view julkaistu_peruste_data_view as
SELECT
    data->'nimi' as nimi,
    data->'kielet' as kielet,
    data->>'voimassaoloAlkaa' as "voimassaoloAlkaa",
    data->>'voimassaoloLoppuu' as "voimassaoloLoppuu",
    data->>'siirtymaPaattyy' as "siirtymaPaattyy",
    data->>'id' as "id",
    data->>'diaarinumero' as diaarinumero,
    data->'osaamisalat' as osaamisalat,
    data->'tutkintonimikkeet' as tutkintonimikkeet,
    data->'tutkinnonosat' as tutkinnonosat,
    p.tila,
    data->>'koulutusvienti' as "koulutusvienti",
    data->>'koulutustyyppi' as "koulutustyyppi",
    data->>'tyyppi' as "tyyppi",
    data->'oppaanKoulutustyypit' as "oppaankoulutustyypit",
    data->'suoritustavat'->0->'rakenne'->'muodostumisSaanto'->'laajuus'->>'minimi' as "laajuus",
    (SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'osaamisalat') tdata) as "osaamisalanimet",
    (SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'tutkintonimikkeet') tdata) as "tutkintonimikkeetnimet",
    (SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'tutkinnonOsat') tdata) as "tutkinnonosatnimet",
    data->'koulutukset' as koulutukset,
    jp.luotu as "julkaistu",
    data->'suoritustavat' as "suoritustavat",
    data->>'luotu' as "luotu",
    COALESCE((SELECT json_agg(tdata->'nimiKoodi'->'uri') from jsonb_array_elements(data->'koulutuksenOsat') tdata)::jsonb, '[]'::jsonb) ||
        COALESCE((SELECT json_agg(tdata->'koodi'->'uri') from jsonb_array_elements(data->'tutkinnonOsat') tdata)::jsonb, '[]'::jsonb) ||
        COALESCE((SELECT json_agg(tdata->'uri') from jsonb_array_elements(data->'osaamisalat') tdata)::jsonb, '[]'::jsonb) ||
        COALESCE((SELECT json_agg(tdata->'tutkintonimikeUri') from jsonb_array_elements(data->'tutkintonimikkeet') tdata)::jsonb, '[]'::jsonb) as "koodit"
FROM julkaistu_peruste jp
    INNER JOIN julkaistu_peruste_data d on d.id = jp.data_id
    INNER JOIN peruste p on p.id = jp.peruste_id
where revision = (SELECT MAX(revision) FROM julkaistu_peruste j2 WHERE jp.peruste_id = j2.peruste_id)
  AND p.tila != 'POISTETTU';

CREATE UNIQUE INDEX ON julkaistu_peruste_data_view ("id");

CREATE OR REPLACE FUNCTION tg_refresh_julkaistu_peruste_data_view()
RETURNS trigger AS
'
BEGIN
	REFRESH MATERIALIZED VIEW CONCURRENTLY julkaistu_peruste_data_view;
	RETURN null;
END
'
LANGUAGE plpgsql;

CREATE TRIGGER tg_refresh_julkaistu_peruste_data_view AFTER INSERT OR UPDATE OR DELETE
                                                                      ON julkaistu_peruste
                                                                          FOR EACH STATEMENT EXECUTE PROCEDURE tg_refresh_julkaistu_peruste_data_view();

CREATE TRIGGER tg_refresh_julkaistu_peruste_data_view
    AFTER UPDATE OF tila ON peruste
    FOR EACH ROW
    EXECUTE PROCEDURE tg_refresh_julkaistu_peruste_data_view();
