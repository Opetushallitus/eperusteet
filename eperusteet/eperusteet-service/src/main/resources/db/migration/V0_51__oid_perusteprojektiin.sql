ALTER TABLE perusteprojekti
    ADD COLUMN oid text UNIQUE;

ALTER TABLE perusteprojekti_aud
    ADD COLUMN oid text UNIQUE;
