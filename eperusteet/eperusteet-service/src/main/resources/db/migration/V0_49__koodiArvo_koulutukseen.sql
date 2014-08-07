ALTER TABLE tutkinnonosa
    RENAME COLUMN koodiArvo to koodi_arvo;
ALTER TABLE tutkinnonosa
    RENAME COLUMN koodiuri to koodi_uri;

ALTER TABLE tutkinnonosa_AUD
    RENAME COLUMN koodiArvo to koodi_arvo;
ALTER TABLE tutkinnonosa_AUD
    RENAME COLUMN koodiuri to koodi_uri;;

ALTER TABLE koulutus
    ADD COLUMN koulutuskoodi_arvo VARCHAR(255);

UPDATE koulutus
    SET koulutuskoodi_arvo = substring(koulutus_koodi from 10 for 15);

ALTER TABLE koulutus
    RENAME COLUMN koulutus_koodi to koulutuskoodi_uri;

ALTER TABLE koulutus
    ALTER COLUMN koulutuskoodi_arvo SET NOT NULL;

ALTER TABLE koulutus ADD CONSTRAINT koulutuskoodi_arvo_unique UNIQUE (koulutuskoodi_arvo);



