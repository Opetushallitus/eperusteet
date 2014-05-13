ALTER TABLE ONLY perusteenosa_aud
        ADD tila VARCHAR(255);

UPDATE perusteenosa_aud SET tila = 'LUONNOS';

ALTER TABLE ONLY peruste_aud
        RENAME COLUMN paivays TO voimassaolo_alkaa;
ALTER TABLE ONLY peruste_aud
        RENAME COLUMN siirtyma TO siirtyma_alkaa;

ALTER TABLE ONLY peruste
        RENAME COLUMN paivays TO voimassaolo_alkaa;
ALTER TABLE ONLY peruste
        RENAME COLUMN siirtyma TO siirtyma_alkaa;

ALTER TABLE ONLY peruste_aud
        ADD COLUMN voimassaolo_loppuu timestamp without time zone;
ALTER TABLE ONLY peruste
        ADD COLUMN voimassaolo_loppuu timestamp without time zone;
