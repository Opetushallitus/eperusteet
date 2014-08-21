ALTER TABLE ONLY tutkinnonosa
    DROP CONSTRAINT fk_perusteenosa_osaamisala_tekstipalanen;

ALTER TABLE ONLY tutkinnonosa
    DROP COLUMN osaamisala_id;

CREATE TABLE osaamisala (
    id bigint NOT NULL PRIMARY KEY,
    nimi_id bigint,
    osaamisalakoodi_arvo VARCHAR(255),
    osaamisalakoodi_uri VARCHAR(255)
);

ALTER TABLE ONLY osaamisala
    ADD CONSTRAINT fk_osaamisala_nimi_tekstipalanen FOREIGN KEY (nimi_id) REFERENCES tekstipalanen(id);

ALTER TABLE tutkinnon_rakenne
    ADD COLUMN osaamisala_id bigint;

ALTER TABLE tutkinnon_rakenne_aud
    ADD COLUMN osaamisala_id bigint;

ALTER TABLE tutkinnon_rakenne
    ADD CONSTRAINT fk_tutkinnon_rakenne_osaamisala FOREIGN KEY (osaamisala_id) REFERENCES osaamisala(id);