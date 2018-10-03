CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

SELECT tunniste INTO fix_duplicates_helper FROM tutkinnon_rakenne GROUP BY tunniste HAVING count(*) > 1;
SELECT id INTO fix_duplicates_id_helper FROM tutkinnon_rakenne WHERE tunniste IN (SELECT tunniste FROM fix_duplicates_helper);

CREATE OR REPLACE FUNCTION fix_tunniste(fixId bigint) RETURNS void AS $$
    DECLARE
        new_tunniste UUID := uuid_generate_v4();
    BEGIN
        UPDATE tutkinnon_rakenne
            SET tunniste = new_tunniste
            WHERE id = fixId;

        UPDATE tutkinnon_rakenne_aud
            SET tunniste = new_tunniste
            WHERE id = fixId;
    END
$$ LANGUAGE plpgsql;

CREATE UNIQUE INDEX IF NOT EXISTS tutkinnon_rakenne_uniikki_tunniste ON tutkinnon_rakenne (tunniste);

DROP TABLE fix_duplicates_helper;
DROP TABLE fix_duplicates_id_helper;
DROP FUNCTION fix_tunniste;
