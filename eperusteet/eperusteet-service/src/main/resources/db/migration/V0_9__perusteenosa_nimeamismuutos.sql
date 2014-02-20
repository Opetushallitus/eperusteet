ALTER TABLE perusteenosa 
        ADD COLUMN nimi_id bigint;

ALTER TABLE tutkinnonosa
        ADD COLUMN osaamisala_id bigint,
        ADD COLUMN opintoluokitus bigint;
        
ALTER TABLE perusteenosa 
        ADD CONSTRAINT fk_perusteenosa_nimi_tekstipalanen
        FOREIGN KEY (nimi_id) 
        REFERENCES tekstipalanen;
        
ALTER TABLE tutkinnonosa 
        ADD CONSTRAINT fk_perusteenosa_osaamisala_tekstipalanen
        FOREIGN KEY (osaamisala_id) 
        REFERENCES tekstipalanen;

UPDATE perusteenosa SET otsikko_id = nimi_id;

ALTER TABLE perusteenosa
        DROP CONSTRAINT fk_perusteenosa_otsikko_tekstipalanen;

ALTER TABLE perusteenosa
        DROP COLUMN otsikko_id;
        