ALTER TABLE tutkinnonosa_osaalue ADD COLUMN tyyppi VARCHAR (20) DEFAULT 'OSAALUE2014';
ALTER TABLE tutkinnonosa_osaalue_aud ADD COLUMN tyyppi  VARCHAR (20) DEFAULT 'OSAALUE2014';

ALTER TABLE tutkinnonosa_osaalue ADD COLUMN geneerinenArviointiasteikko_id BIGINT REFERENCES geneerinenarviointiasteikko(id);
ALTER TABLE tutkinnonosa_osaalue_aud ADD COLUMN geneerinenArviointiasteikko_id BIGINT;

ALTER TABLE tutkinnonosa_osaalue ADD COLUMN pakollisetOsaamistavoitteet_id BIGINT REFERENCES osaamistavoite(id);
ALTER TABLE tutkinnonosa_osaalue_aud ADD COLUMN pakollisetOsaamistavoitteet_id BIGINT;

ALTER TABLE tutkinnonosa_osaalue ADD COLUMN valinnaisetOsaamistavoitteet_id BIGINT REFERENCES osaamistavoite(id);
ALTER TABLE tutkinnonosa_osaalue_aud ADD COLUMN valinnaisetOsaamistavoitteet_id BIGINT;

ALTER TABLE osaamistavoite ADD COLUMN tavoitteet2020_id BIGINT REFERENCES ammattitaitovaatimukset2019(id);
ALTER TABLE osaamistavoite_aud ADD COLUMN tavoitteet2020_id BIGINT;
