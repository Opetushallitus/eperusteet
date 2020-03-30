UPDATE peruste SET koulutustyyppi = 'koulutustyyppi_1' WHERE tyyppi = 'OPAS' AND koulutustyyppi IS NULL;

create table tekstikappale_koodi (
    tekstikappale_id int8 not null,
    koodi_id int8 not null
);

create table tekstikappale_koodi_AUD (
    REV int4 not null,
    tekstikappale_id int8 not null,
    koodi_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tekstikappale_id, koodi_id)
);

alter table tekstikappale_koodi
    add constraint FK_k0daabryvw3txwf19tqv7eeol
    foreign key (koodi_id)
    references koodi;

alter table tekstikappale_koodi
    add constraint FK_r2hxe8aii6yy9bou7xxwqxmcr
    foreign key (tekstikappale_id)
    references tekstikappale;

alter table tekstikappale_koodi_AUD
    add constraint FK_kt8gqyt49fkcsk52e3j63ct3g
    foreign key (REV)
    references revinfo;

alter table tekstikappale_koodi_AUD
    add constraint FK_1eiet16enb7c6xgcnjg3gqc1d
    foreign key (REVEND)
    references revinfo;
