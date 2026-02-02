UPDATE peruste SET koulutustyyppi = 'koulutustyyppi_500', toteutus = 'KAANTAJATUTKINTO' WHERE tyyppi = 'KIELI_KAANTAJA_TUTKINTO';

insert into maarays_koulutustyypit select id, 'koulutustyyppi_500' from maarays where peruste_id in (select id from peruste where tyyppi = 'KIELI_KAANTAJA_TUTKINTO');