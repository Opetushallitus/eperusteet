CREATE INDEX maarays_nimi_id_index ON maarays(nimi_id);
CREATE INDEX maarays_koulutustyypit_index ON maarays_koulutustyypit(koulutustyypit);
CREATE INDEX maarays_tyyppi_index ON maarays(tyyppi);
CREATE INDEX maarays_asiasanat_kieli_index ON maarays_asiasanat(asiasanat_KEY);
CREATE INDEX maarays_asiasana_asiasana_index ON maarays_asiasana_asiasana(asiasana);