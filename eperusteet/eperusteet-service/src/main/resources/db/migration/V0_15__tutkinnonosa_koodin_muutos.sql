ALTER TABLE tutkinnonosa
    ADD COLUMN koodiUri VARCHAR(255);

ALTER TABLE tutkinnonosa
	DROP COLUMN koodi;
	
ALTER TABLE tutkinnonosa_AUD
    ADD COLUMN koodiUri VARCHAR(255);

ALTER TABLE tutkinnonosa_AUD
	DROP COLUMN koodi;