ALTER TABLE perusteprojekti ADD COLUMN kuvaus_id BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE perusteprojekti_aud ADD COLUMN kuvaus_id BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE perusteprojekti ADD COLUMN projekti_kuvaus VARCHAR(255);
ALTER TABLE perusteprojekti_aud ADD COLUMN projekti_kuvaus VARCHAR(255);
