ALTER TABLE tiedote ADD COLUMN yleinen boolean NOT NULL DEFAULT TRUE;
ALTER TABLE tiedote_aud ADD COLUMN yleinen boolean;
