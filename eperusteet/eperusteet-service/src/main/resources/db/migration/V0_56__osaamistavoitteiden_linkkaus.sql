ALTER TABLE ONLY osaamistavoite
        ADD esitieto_id bigint;
ALTER TABLE ONLY osaamistavoite_aud
        ADD esitieto_id bigint;

ALTER TABLE ONLY osaamistavoite
    ADD CONSTRAINT fk_osaamistavoite_esitieto FOREIGN KEY (esitieto_id) REFERENCES osaamistavoite(id);

ALTER TABLE ONLY osaamistavoite
    ADD CONSTRAINT esitietoa_ei_voi_antaa_pakolliselle CHECK (pakollinen IS FALSE OR esitieto_id IS NULL);
