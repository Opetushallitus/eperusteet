ALTER TABLE ONLY perusteprojekti
    ADD COLUMN tehtava VARCHAR(255),
    ADD COLUMN toimikausi_alku timestamp without time zone,
    ADD COLUMN toimikausi_loppu timestamp without time zone,
    ADD COLUMN yhteistyotaho VARCHAR(255),
    ADD CONSTRAINT UK_perusteprojekti_diaarinumero UNIQUE (diaarinumero),
    ALTER COLUMN diaarinumero SET NOT NULL;