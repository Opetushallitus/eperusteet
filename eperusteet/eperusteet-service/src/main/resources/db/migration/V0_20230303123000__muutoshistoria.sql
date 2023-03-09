ALTER TABLE julkaistu_peruste ADD COLUMN julkinen boolean NOT NULL DEFAULT FALSE;
ALTER TABLE julkaistu_peruste ADD COLUMN muutosmaarays_voimaan TIMESTAMP;
ALTER TABLE julkaistu_peruste ADD COLUMN julkinen_tiedote_id bigint;
ALTER TABLE julkaistu_peruste
    ADD CONSTRAINT FK_julkinen_tiedote
        FOREIGN KEY (julkinen_tiedote_id)
            REFERENCES tekstipalanen;

CREATE TABLE julkaistu_peruste_muutosmaarays(julkaistu_peruste_id bigint NOT NULL REFERENCES julkaistu_peruste(id),
                                             muutosmaaraykset_id bigint NOT NULL REFERENCES muutosmaarays(id),
                                            muutosmaaraykset_order INTEGER NOT NULL);

CREATE TABLE julkaistu_peruste_muutosmaarays_aud(julkaistu_peruste_id bigint NOT NULL,
                                                 muutosmaaraykset_id bigint NOT NULL,
                                                 muutosmaaraykset_order INTEGER NOT NULL,
                                                 rev INTEGER,
                                                 revend INTEGER,
                                                 revtype SMALLINT);
