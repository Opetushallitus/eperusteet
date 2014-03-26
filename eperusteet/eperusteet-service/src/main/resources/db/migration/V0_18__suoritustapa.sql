CREATE TABLE suoritustapa (
    id bigint NOT NULL PRIMARY KEY,
    suoritustapakoodi character varying(255) NOT NULL,
    sisalto_perusteenosaviite_id bigint 
);

ALTER TABLE ONLY suoritustapa
    ADD CONSTRAINT fk_suoritustapa_perusteenosaviite FOREIGN KEY (sisalto_perusteenosaviite_id) REFERENCES perusteenosaviite(id);

ALTER TABLE ONLY peruste
    DROP CONSTRAINT IF EXISTS fk_peruste_perusteenosaviite;

ALTER TABLE ONLY peruste
    DROP IF EXISTS rakenne_id;

CREATE TABLE peruste_suoritustapa (
    peruste_id bigint REFERENCES peruste(id),
    suoritustapa_id bigint REFERENCES suoritustapa(id)
);


ALTER TABLE suoritustapa
    ADD COLUMN peruste_id bigint;
insert into suoritustapa(id, suoritustapakoodi, peruste_id) select nextval('hibernate_sequence') as id, 'ops' as suoritustapakoodi, p.id from peruste p where p.tutkintokoodi='koulutustyyppi_1';
insert into suoritustapa(id, suoritustapakoodi, peruste_id) select nextval('hibernate_sequence') as id, 'naytto' as suoritustapakoodi, p.id from peruste p;
insert into peruste_suoritustapa(peruste_id, suoritustapa_id) select s.peruste_id, s.id from suoritustapa s;
ALTER TABLE ONLY suoritustapa
    DROP IF EXISTS peruste_id;