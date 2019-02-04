CREATE TABLE maarayskirje_status (
    id BIGINT NOT NULL,
    peruste_id BIGINT NOT NULL,
    aikaleima TIMESTAMP NOT NULL,
    lataaminen_ok boolean not null,
    primary key (id)
);
