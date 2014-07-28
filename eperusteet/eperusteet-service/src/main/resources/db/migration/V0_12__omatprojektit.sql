CREATE TABLE kayttajaprofiili_perusteprojekti (
    kayttajaprofiili_id bigint REFERENCES kayttajaprofiili(id),
    perusteprojekti_id bigint REFERENCES perusteprojekti(id),
    projekti_order integer
);