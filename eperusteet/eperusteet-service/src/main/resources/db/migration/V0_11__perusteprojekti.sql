CREATE TABLE perusteprojekti(
    id bigint NOT NULL PRIMARY KEY,
    nimi varchar(255),
    peruste_id bigint,
    diaarinumero varchar(255),
    paatosPvm timestamp without time zone,
    tehtavaluokka varchar(255)
);

ALTER TABLE ONLY perusteprojekti
    ADD CONSTRAINT fk_perusteprojekti_peruste FOREIGN KEY (peruste_id) REFERENCES peruste(id);