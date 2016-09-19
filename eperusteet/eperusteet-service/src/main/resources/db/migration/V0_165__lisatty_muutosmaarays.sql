CREATE TABLE muutosmaarays (
    id BIGINT NOT NULL,
    peruste_id BIGINT NOT NULL REFERENCES peruste(id),
    nimi_id BIGINT REFERENCES tekstipalanen(id),
    url_id BIGINT REFERENCES tekstipalanen(id),
    PRIMARY KEY (id, peruste_id, url_id)
);

CREATE TABLE muutosmaarays_aud (
    id BIGINT NOT NULL,
    peruste_id BIGINT NOT NULL,
    nimi_id BIGINT,
    url_id BIGINT,
    REV INTEGER NOT NULL,
    REVTYPE SMALLINT,
    REVEND INTEGER,
    PRIMARY KEY (id, peruste_id, url_id, REV)
);
