CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

update tutkinnon_rakenne set tunniste = uuid_generate_v4() where tunniste in (select tunniste from tutkinnon_rakenne group by tunniste HAVING count(*) > 1);

CREATE UNIQUE INDEX IF NOT EXISTS tutkinnon_rakenne_uniikki_tunniste ON tutkinnon_rakenne (tunniste);
