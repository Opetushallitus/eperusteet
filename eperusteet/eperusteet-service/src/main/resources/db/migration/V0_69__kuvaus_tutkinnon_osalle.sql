alter table only tutkinnonosa
        add kuvaus_id bigint;
alter table only tutkinnonosa_aud
        add kuvaus_id bigint;

ALTER TABLE ONLY tutkinnonosa
    ADD CONSTRAINT fk_tutkinnonosa_kuvaus_tekstipalanen FOREIGN KEY (kuvaus_id) REFERENCES tekstipalanen(id);