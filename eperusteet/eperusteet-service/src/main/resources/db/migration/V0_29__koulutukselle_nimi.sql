ALTER TABLE ONLY koulutus
        add nimi_id bigint;

ALTER TABLE ONLY koulutus
    ADD CONSTRAINT fk_koulutus_nimi_tekstipalanen FOREIGN KEY (nimi_id) REFERENCES tekstipalanen(id);
