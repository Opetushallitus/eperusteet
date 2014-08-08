ALTER TABLE peruste
    ADD COLUMN tyyppi VARCHAR(255);

ALTER TABLE peruste_aud
    ADD COLUMN tyyppi VARCHAR(255);

update peruste set tyyppi = 'NORMAALI' WHERE tila != 'POHJA' AND tila != 'POISTETTUPOHJA';
update peruste set tyyppi = 'POHJA' WHERE tila = 'POHJA' OR tila = 'POISTETTUPOHJA';
update peruste set tila = 'VALMIS' WHERE tila = 'POHJA';
update peruste set tila = 'POISTETTU' WHERE tila = 'POISTETTUPOHJA';

update peruste_aud set tyyppi = 'NORMAALI' WHERE tila != 'POHJA' AND tila != 'POISTETTUPOHJA';
update peruste_aud set tyyppi = 'POHJA' WHERE tila = 'POHJA' OR tila = 'POISTETTUPOHJA';
update peruste_aud set tila = 'VALMIS' WHERE tila = 'POHJA';
update peruste_aud set tila = 'POISTETTU' WHERE tila = 'POISTETTUPOHJA';

ALTER TABLE peruste
    ALTER COLUMN tyyppi SET NOT NULL;

ALTER TABLE peruste_aud
    ALTER COLUMN tyyppi SET NOT NULL;