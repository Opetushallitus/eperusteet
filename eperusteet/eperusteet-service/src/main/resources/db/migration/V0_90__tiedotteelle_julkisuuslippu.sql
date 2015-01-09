ALTER TABLE tiedote ADD COLUMN julkinen BOOLEAN;
ALTER TABLE tiedote_aud ADD COLUMN julkinen BOOLEAN;

UPDATE tiedote SET julkinen = TRUE;

ALTER TABLE tiedote
    ALTER COLUMN julkinen SET NOT NULL;
