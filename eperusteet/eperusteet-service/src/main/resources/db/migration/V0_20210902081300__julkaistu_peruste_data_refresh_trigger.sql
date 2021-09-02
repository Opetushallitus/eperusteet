drop trigger if exists tg_refresh_julkaistu_peruste_data_view on julkaistu_peruste;
drop trigger if exists tg_refresh_julkaistu_peruste_data_view on peruste;
drop function if exists tg_refresh_julkaistu_peruste_data_view;

CREATE OR REPLACE FUNCTION tg_refresh_julkaistu_peruste_data_view()
RETURNS trigger AS
'
BEGIN
	REFRESH MATERIALIZED VIEW julkaistu_peruste_data_view;
	RETURN null;
END
'
LANGUAGE plpgsql;

CREATE TRIGGER tg_refresh_julkaistu_peruste_data_view AFTER INSERT OR UPDATE OR DELETE
ON julkaistu_peruste
FOR EACH STATEMENT EXECUTE PROCEDURE tg_refresh_julkaistu_peruste_data_view();

CREATE TRIGGER tg_refresh_julkaistu_peruste_data_view AFTER UPDATE
ON peruste
FOR EACH STATEMENT EXECUTE PROCEDURE tg_refresh_julkaistu_peruste_data_view();
