ALTER TABLE julkaistu_peruste_data_store
ALTER COLUMN osaamisalanimet
SET DATA TYPE jsonb
USING osaamisalanimet::jsonb;

ALTER TABLE julkaistu_peruste_data_store
ALTER COLUMN tutkintonimikkeetnimet
SET DATA TYPE jsonb
USING tutkintonimikkeetnimet::jsonb;

ALTER TABLE julkaistu_peruste_data_store
ALTER COLUMN tutkinnonosatnimet
SET DATA TYPE jsonb
USING tutkinnonosatnimet::jsonb;

CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_nimi_fi ON julkaistu_peruste_data_store ((nimi->>'fi'));
CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_nimi_sv ON julkaistu_peruste_data_store ((nimi->>'sv'));
CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_nimi_en ON julkaistu_peruste_data_store ((nimi->>'en'));

CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_tyyppi_btree ON julkaistu_peruste_data_store (tyyppi);
CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_diaarinumero_btree ON julkaistu_peruste_data_store (diaarinumero);
CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_voimassaoloalkaa_btree ON julkaistu_peruste_data_store ("voimassaoloAlkaa");
CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_voimassaololoppuu_btree ON julkaistu_peruste_data_store ("voimassaoloLoppuu");
CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_siirtymapaattyy_btree ON julkaistu_peruste_data_store ("siirtymaPaattyy");
CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_sisaltotyyppi_btree ON julkaistu_peruste_data_store (sisaltotyyppi);
CREATE INDEX IF NOT EXISTS idx_julkaistu_peruste_data_store_koulutustyyppi_btree ON julkaistu_peruste_data_store (koulutustyyppi);