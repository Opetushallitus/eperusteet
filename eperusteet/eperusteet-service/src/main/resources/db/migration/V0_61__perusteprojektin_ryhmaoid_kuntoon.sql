ALTER TABLE perusteprojekti_aud ADD COLUMN ryhmaoid text;
ALTER TABLE perusteprojekti DROP CONSTRAINT IF EXISTS perusteprojekti_oid_key;

UPDATE perusteprojekti SET ryhmaoid = oid;
ALTER TABLE perusteprojekti DROP CONSTRAINT IF EXISTS perusteprojekti_ryhmaoid_key;

ALTER TABLE perusteprojekti DROP COLUMN oid;
ALTER TABLE perusteprojekti_aud DROP COLUMN oid;
