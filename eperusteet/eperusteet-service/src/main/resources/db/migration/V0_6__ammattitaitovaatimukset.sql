ALTER TABLE ONLY tutkinnonosa
    ADD COLUMN ammattitaitovaatimukset_id bigint,
    ADD COLUMN ammattitaidonOsoittamistavat_id bigint;

ALTER TABLE ONLY tutkinnonosa
    ADD CONSTRAINT fk_tutkinnonosa_ammattitaitovaatimukset_tekstipalanen FOREIGN KEY (ammattitaitovaatimukset_id) REFERENCES tekstipalanen(id),
    ADD CONSTRAINT fk_tutkinnonosa_ammattitaidonOsoittamistavat_tekstipalanen FOREIGN KEY (ammattitaidonOsoittamistavat_id) REFERENCES tekstipalanen(id);

