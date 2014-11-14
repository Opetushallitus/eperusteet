UPDATE tutkinnonosaviite SET poistettu = '0' WHERE poistettu IS NULL;
UPDATE tutkinnonosaviite_aud SET poistettu = '0' WHERE poistettu IS NULL;

ALTER TABLE ONLY tutkinnonosaviite
        ALTER poistettu SET NOT NULL;

ALTER TABLE ONLY tutkinnonosaviite_aud
        ALTER poistettu SET NOT NULL;