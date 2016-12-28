-- Tehdään korjauksia muutosmääräyksiin
ALTER TABLE muutosmaarays DROP CONSTRAINT muutosmaarays_pkey;
ALTER TABLE muutosmaarays ADD CONSTRAINT muutosmaarays_pkey PRIMARY KEY (id);

ALTER TABLE muutosmaarays_aud DROP CONSTRAINT muutosmaarays_aud_pkey;
ALTER TABLE muutosmaarays_aud ADD CONSTRAINT muutosmaarays_aud_pkey PRIMARY KEY (id, REV);

ALTER TABLE muutosmaarays_aud ALTER COLUMN peruste_id DROP NOT NULL;
ALTER TABLE muutosmaarays_aud ALTER COLUMN url_id DROP NOT NULL;

ALTER TABLE muutosmaarays ADD CONSTRAINT fk_muutosmaarays_peruste UNIQUE (id);

-- Luodaan välitaulu muutosmääräyksiä varten
CREATE TABLE peruste_muutosmaarays(
    peruste_id bigint NOT NULL REFERENCES peruste(id),
    muutosmaaraykset_id bigint NOT NULL REFERENCES muutosmaarays(id),
    muutosmaaraykset_order integer not null
);

CREATE TABLE peruste_muutosmaarays_aud(
    peruste_id bigint NOT NULL,
    muutosmaaraykset_id bigint NOT NULL,
    muutosmaaraykset_order integer not null,
    rev integer,
    revend integer,
    revtype smallint
);

-- Lisätään olemassa olevat muutosmääräykset välitauluun
INSERT INTO peruste_muutosmaarays(peruste_id, muutosmaaraykset_id, muutosmaaraykset_order)
    SELECT peruste_id, id, 0 FROM muutosmaarays;