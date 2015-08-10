ALTER TABLE perusteprojekti DROP CONSTRAINT uk_perusteprojekti_diaarinumero;
CREATE UNIQUE INDEX uk_perusteprojekti_diaarinumero_tila ON perusteprojekti(diaarinumero, tila);
