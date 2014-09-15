CREATE TABLE perusteprojekti_tyoryhma (
    id bigint NOT NULL PRIMARY KEY,
    kayttaja_oid text NOT NULL,
    nimi text NOT NULL,
    perusteprojekti_id bigint NOT NULL REFERENCES perusteprojekti(id),
    CONSTRAINT perusteprojekti_tyoryhma_unique_fields UNIQUE(kayttaja_oid, nimi, perusteprojekti_id)
);