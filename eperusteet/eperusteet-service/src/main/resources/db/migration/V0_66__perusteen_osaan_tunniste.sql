ALTER TABLE perusteenosa ADD COLUMN tunniste VARCHAR(255);

ALTER TABLE perusteenosa_aud ADD COLUMN tunniste VARCHAR(255);

-- Magic migration
ALTER TABLE perusteenosa ADD COLUMN vanhin_id_temp bigint;
WITH po AS (
    INSERT INTO perusteenosa
    SELECT nextval('hibernate_sequence'), now(), now(), null, null, null, 'LUONNOS', 'RAKENNE', s.sisalto_perusteenosaviite_id
    FROM suoritustapa s
    RETURNING *
)
INSERT INTO perusteenosaviite
SELECT nextval('hibernate_sequence'), po.id, po.vanhin_id_temp, 0
FROM po;
ALTER TABLE perusteenosa DROP COLUMN vanhin_id_temp;
