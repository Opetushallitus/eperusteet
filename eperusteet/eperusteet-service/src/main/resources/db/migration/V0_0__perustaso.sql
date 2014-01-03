CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE peruste (
    id bigint NOT NULL PRIMARY KEY,
    koulutusalakoodi character varying(255),
    paivays timestamp without time zone,
    tutkintokoodi character varying(255),
    nimi_id bigint,
    rakenne_id bigint
);

CREATE TABLE perusteenosa (
    id bigint NOT NULL PRIMARY KEY,
    luotu timestamp without time zone,
    muokattu timestamp without time zone,
    otsikko_id bigint
);


CREATE TABLE perusteenosaviite (
    id bigint NOT NULL PRIMARY KEY,
    perusteenosa_id bigint,
    vanhempi_id bigint,
    lapset_order integer
);

CREATE TABLE tekstikappale (
    id bigint NOT NULL PRIMARY KEY,
    teksti_id bigint
);


CREATE TABLE tekstipalanen (
    id bigint NOT NULL PRIMARY KEY
);


CREATE TABLE tekstipalanen_teksti (
    tekstipalanen_id bigint NOT NULL REFERENCES tekstipalanen (id),
    kieli character varying(2) NOT NULL,
    teksti text NOT NULL
);

CREATE UNIQUE INDEX ix_tekstipalanen_teksti 
  ON tekstipalanen_teksti(tekstipalanen_id, kieli);

CREATE TABLE tutkinnonosa (
    id bigint NOT NULL PRIMARY KEY,
    tavoitteet_id bigint
);

ALTER TABLE ONLY perusteenosaviite
    ADD CONSTRAINT fk_perusteenosaviite_vanhempi FOREIGN KEY (vanhempi_id) REFERENCES perusteenosaviite(id);

ALTER TABLE ONLY tekstikappale
    ADD CONSTRAINT fk_tekstikappale_teksti_tekstipalanen FOREIGN KEY (teksti_id) REFERENCES tekstipalanen(id);

ALTER TABLE ONLY peruste
    ADD CONSTRAINT fk_peruste_perusteenosaviite FOREIGN KEY (rakenne_id) REFERENCES perusteenosaviite(id);

ALTER TABLE ONLY tekstikappale
    ADD CONSTRAINT fk_tekstikappale_perusteenosa FOREIGN KEY (id) REFERENCES perusteenosa(id);

ALTER TABLE ONLY tutkinnonosa
    ADD CONSTRAINT fk_tutkinnonosa_tavoitteet_tekstipalanen FOREIGN KEY (tavoitteet_id) REFERENCES tekstipalanen(id);

ALTER TABLE ONLY perusteenosa
    ADD CONSTRAINT fk_perusteenosa_otsikko_tekstipalanen FOREIGN KEY (otsikko_id) REFERENCES tekstipalanen(id);

ALTER TABLE ONLY perusteenosaviite
    ADD CONSTRAINT fk_perusteenosaviite_perusteenosa FOREIGN KEY (perusteenosa_id) REFERENCES perusteenosa(id);

ALTER TABLE ONLY tutkinnonosa
    ADD CONSTRAINT fk_tutkinnonosa_perusteenosa FOREIGN KEY (id) REFERENCES perusteenosa(id);

ALTER TABLE ONLY peruste
    ADD CONSTRAINT fk_peruste_nimi_tekstipalanen FOREIGN KEY (nimi_id) REFERENCES tekstipalanen(id);
