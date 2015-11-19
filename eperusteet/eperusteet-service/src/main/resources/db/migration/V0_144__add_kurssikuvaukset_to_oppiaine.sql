-- oppiaineeseen kurssien kuvaustiedot
ALTER TABLE yl_oppiaine ADD COLUMN pakollinen_kurssi_kuvaus BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE yl_oppiaine ADD COLUMN syventava_kurssi_kuvaus BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE yl_oppiaine ADD COLUMN soveltava_kurssi_kuvaus BIGINT REFERENCES tekstipalanen(id);

ALTER TABLE yl_oppiaine_aud ADD COLUMN pakollinen_kurssi_kuvaus BIGINT;
ALTER TABLE yl_oppiaine_aud ADD COLUMN syventava_kurssi_kuvaus BIGINT;
ALTER TABLE yl_oppiaine_aud ADD COLUMN soveltava_kurssi_kuvaus BIGINT;
