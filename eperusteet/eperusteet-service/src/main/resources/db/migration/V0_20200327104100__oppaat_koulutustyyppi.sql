UPDATE peruste SET koulutustyyppi = 'koulutustyyppi_1' WHERE tyyppi = 'OPAS' AND koulutustyyppi IS NULL;

ALTER TABLE tekstikappale ADD COLUMN tutkinnonosa_id int8;
ALTER TABLE tekstikappale_aud ADD COLUMN tutkinnonosa_id int8;

alter table tekstikappale
    add constraint FK_9hc0iolbqc6h2ffb5jyy3563e
    foreign key (tutkinnonosa_id)
    references koodi;
