ALTER TABLE tiedote ADD COLUMN perusteprojekti_id bigint REFERENCES perusteprojekti(id);
ALTER TABLE tiedote_aud ADD COLUMN perusteprojekti_id bigint REFERENCES perusteprojekti(id);
