drop trigger if exists tg_refresh_julkaistu_peruste_data_view on peruste;

CREATE TRIGGER tg_refresh_julkaistu_peruste_data_view
    AFTER UPDATE ON peruste
    FOR EACH ROW
    WHEN (NEW.tila <> OLD.tila)
    EXECUTE PROCEDURE tg_refresh_julkaistu_peruste_data_view();