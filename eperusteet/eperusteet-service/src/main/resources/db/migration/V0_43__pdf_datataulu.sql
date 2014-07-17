create table dokumentti (
    id              bigint not null primary key,
    peruste_id      bigint,
    kieli           character varying not null,
    luoja           character varying not null, 
    aloitusaika     timestamp without time zone not null,
    valmistumisaika timestamp without time zone,
    tila            character varying not null,
    dokumenttidata  oid,
    virhekoodi      character varying not null   
);

ALTER TABLE ONLY dokumentti
    ADD CONSTRAINT fk_dokumentti_peruste FOREIGN KEY (peruste_id) REFERENCES peruste(id);
