CREATE TABLE koodi(
    id bigint NOT NULL PRIMARY KEY,
    nimi_id bigint NOT NULL,
    arvo VARCHAR(255) NOT NULL,
    uri VARCHAR(255) NOT NULL
);

ALTER TABLE tutkinnon_rakenne ADD COLUMN erikoisuus VARCHAR(255);
ALTER TABLE tutkinnon_rakenne_aud ADD COLUMN erikoisuus VARCHAR(255);

ALTER TABLE tutkinnon_rakenne ADD COLUMN vieras_id bigint REFERENCES koodi(id);
ALTER TABLE tutkinnon_rakenne_aud ADD COLUMN vieras_id bigint REFERENCES koodi(id);
