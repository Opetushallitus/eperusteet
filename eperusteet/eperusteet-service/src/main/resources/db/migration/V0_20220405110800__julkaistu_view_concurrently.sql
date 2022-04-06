CREATE UNIQUE INDEX ON julkaistu_peruste_data_view ("perusteId");

CREATE OR REPLACE FUNCTION tg_refresh_julkaistu_peruste_data_view()
RETURNS trigger AS
'
BEGIN
	REFRESH MATERIALIZED VIEW CONCURRENTLY julkaistu_peruste_data_view;
	RETURN null;
END
'
LANGUAGE plpgsql;
