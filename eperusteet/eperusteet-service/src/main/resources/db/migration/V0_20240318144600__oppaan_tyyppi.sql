ALTER TABLE peruste ADD COLUMN opastyyppi varchar(255);
ALTER TABLE peruste_aud ADD COLUMN opastyyppi varchar(255);

ALTER TABLE peruste ADD COLUMN tietoa_palvelusta_kuvaus BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE peruste_aud ADD COLUMN tietoa_palvelusta_kuvaus BIGINT REFERENCES tekstipalanen(id);