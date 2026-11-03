alter table taiteenalavaihe
    add column laajuus_yksikko varchar(255);

alter table taiteenalavaihe_AUD
    add column laajuus_yksikko varchar(255);

update peruste set toteutus = 'TPO' where koulutustyyppi = 'koulutustyyppi_999907' and toteutus is null;

update peruste_AUD set toteutus = 'TPO' where koulutustyyppi = 'koulutustyyppi_999907' and toteutus is null;
