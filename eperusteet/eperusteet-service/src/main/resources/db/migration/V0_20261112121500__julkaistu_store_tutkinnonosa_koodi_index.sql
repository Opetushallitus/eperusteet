CREATE INDEX IF NOT EXISTS idx_julkaistu_store_tutkinnonosa_koodi
    ON julkaistu_peruste_data_store ((koodit ->> 0))
    WHERE sisaltotyyppi = 'tutkinnonosa';
