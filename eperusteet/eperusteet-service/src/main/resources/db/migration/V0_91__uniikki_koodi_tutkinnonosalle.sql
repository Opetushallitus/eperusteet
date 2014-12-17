
ALTER TABLE tutkinnonosa
    ADD CONSTRAINT uk_uniikki_tutkinnonosan_koodi UNIQUE (koodi_uri);
