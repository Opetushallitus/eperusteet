CREATE TABLE kayttajaprofiili_preferenssi(
    id bigint NOT NULL UNIQUE,
    kayttajaprofiili_id bigint NOT NULL REFERENCES kayttajaprofiili(id),
    avain VARCHAR(64),
    arvo VARCHAR(64),
    CONSTRAINT kayttajaprofiili_preferenssi_unique_constraint UNIQUE(kayttajaprofiili_id, avain)
)
