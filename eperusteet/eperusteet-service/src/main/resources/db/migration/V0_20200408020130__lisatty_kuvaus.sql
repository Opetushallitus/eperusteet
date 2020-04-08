ALTER TABLE perusteprojekti ADD COLUMN kuvaus_id BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE perusteprojekti_aud ADD COLUMN kuvaus_id BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE perusteprojekti ADD COLUMN tyyppi VARCHAR(255);
ALTER TABLE perusteprojekti_aud ADD COLUMN tyyppi VARCHAR(255);
