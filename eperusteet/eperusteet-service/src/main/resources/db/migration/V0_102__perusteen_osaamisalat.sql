CREATE TABLE peruste_osaamisala(
    peruste_id bigint NOT NULL REFERENCES peruste(id),
    osaamisala_id bigint NOT NULL REFERENCES koodi(id),
    CONSTRAINT peruste_osaamisala_unique UNIQUE(peruste_id, osaamisala_id)
);

CREATE TABLE peruste_osaamisala_aud(
    peruste_id bigint NOT NULL,
    osaamisala_id bigint NOT NULL,
    rev integer,
    revend integer,
    revtype smallint
);

