CREATE TABLE perusteenosa_tyoryhma (
    id bigint NOT NULL PRIMARY KEY,
    perusteenosa_id bigint NOT NULL REFERENCES perusteenosa(id),
    nimi text NOT NULL,
    perusteprojekti_id bigint NOT NULL REFERENCES perusteprojekti(id),

    CONSTRAINT perusteenosa_tyoryhma_unique_fields UNIQUE(perusteenosa_id, nimi, perusteprojekti_id)
);
