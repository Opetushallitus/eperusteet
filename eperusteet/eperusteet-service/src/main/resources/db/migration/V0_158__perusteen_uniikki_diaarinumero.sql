-- Perusteprojektin diaarinumeron ei pidä olla uniikki (EP-577), mutta perusteen diaarinumeron pitää jos tila on sama.
CREATE UNIQUE INDEX ui_peruste_diaarinumero_tila ON peruste(diaarinumero, tila);