UPDATE tutkinnonosa SET koodi_uri = NULL WHERE koodi_uri = '';

ALTER TABLE tutkinnonosa
    ADD CONSTRAINT uk_uniikki_tutkinnonosan_koodi UNIQUE (koodi_uri);
