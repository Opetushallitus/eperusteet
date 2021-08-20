drop materialized view if exists julkaistu_peruste_data_view;

create materialized view julkaistu_peruste_data_view as
	SELECT
       data->'nimi' as nimi,
       data->>'voimassaoloAlkaa' as "voimassaoloAlkaa",
       data->>'voimassaoloLoppuu' as "voimassaoloLoppuu",
       data->>'siirtymaPaattyy' as "siirtymaPaattyy",
       data->>'id' as "perusteId",
       data->>'diaarinumero' as diaarinumero,
       data->'osaamisalat' as osaamisalat,
       data->'tutkintonimikkeet' as tutkintonimikkeet,
       data->'tutkinnonosat' as tutkinnonosat,
       p.tila,
       data->>'koulutusvienti' as "koulutusvienti",
       data->>'koulutustyyppi' as "koulutustyyppi",
       (SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'osaamisalat') tdata) as "osaamisalanimet",
       (SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'tutkintonimikkeet') tdata) as "tutkintonimikkeetnimet",
       (SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'tutkinnonosat') tdata) as "tutkinnonosatnimet"
       FROM julkaistu_peruste jp
	INNER JOIN julkaistu_peruste_data d on d.id = jp.data_id
	INNER JOIN peruste p on p.id = jp.peruste_id
	where revision = (SELECT MAX(revision) FROM julkaistu_peruste j2 WHERE jp.peruste_id = j2.peruste_id);
