-- EP-577: halutaan luoda perusteprojekteja samoilla diaarinumeroilla, molemmat projektit voivat olla työn alla eli samassa tilassa. Vanha indeksi on unique, korvataan ei-uniquella.
DROP INDEX uk_perusteprojekti_diaarinumero_tila;
CREATE INDEX uk_perusteprojekti_diaarinumero_tila ON perusteprojekti(diaarinumero, tila);

