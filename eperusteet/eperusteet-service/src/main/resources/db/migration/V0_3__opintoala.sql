CREATE TABLE opintoala (
    id bigint NOT NULL PRIMARY KEY,
    koodi character varying(255)
);

CREATE TABLE koulutusala (
    id bigint NOT NULL PRIMARY KEY,
    koodi character varying(255)
);

CREATE TABLE koulutusala_opintoala (
    koulutusala_id bigint REFERENCES koulutusala(id),
    opintoala_id bigint REFERENCES opintoala(id)
);

CREATE TABLE peruste_opintoala (
    peruste_id bigint REFERENCES peruste(id),
    opintoala_id bigint REFERENCES opintoala(id)
);

ALTER TABLE ONLY peruste
    ADD COLUMN koulutusala_id bigint;

ALTER TABLE peruste
    DROP IF EXISTS koulutusalakoodi;

--INSERT INTO koulutusala(id, koodi)
--    SELECT nextval('hibernate_sequence'), koodi FROM (SELECT DISTINCT koulutusalakoodi FROM peruste);

INSERT INTO koulutusala(id, koodi) VALUES
    (nextval('hibernate_sequence'), '1'),
    (nextval('hibernate_sequence'), '2'),
    (nextval('hibernate_sequence'), '3'),
    (nextval('hibernate_sequence'), '4'),
    (nextval('hibernate_sequence'), '5'),
    (nextval('hibernate_sequence'), '6'),
    (nextval('hibernate_sequence'), '7'),
    (nextval('hibernate_sequence'), '8');

INSERT INTO opintoala(id, koodi) VALUES
    (nextval('hibernate_sequence'), '1'),
    (nextval('hibernate_sequence'), '2'),
    (nextval('hibernate_sequence'), '3'),
    (nextval('hibernate_sequence'), '4'),
    (nextval('hibernate_sequence'), '5'),
    (nextval('hibernate_sequence'), '6'),
    (nextval('hibernate_sequence'), '7'),
    (nextval('hibernate_sequence'), '8'),
    (nextval('hibernate_sequence'), '9'),
    (nextval('hibernate_sequence'), '10'),
    (nextval('hibernate_sequence'), '11'),
    (nextval('hibernate_sequence'), '12'),
    (nextval('hibernate_sequence'), '13'),
    (nextval('hibernate_sequence'), '14'),
    (nextval('hibernate_sequence'), '15'),
    (nextval('hibernate_sequence'), '16'),
    (nextval('hibernate_sequence'), '17'),
    (nextval('hibernate_sequence'), '18'),
    (nextval('hibernate_sequence'), '19'),
    (nextval('hibernate_sequence'), '20'),
    (nextval('hibernate_sequence'), '21'),
    (nextval('hibernate_sequence'), '22'),
    (nextval('hibernate_sequence'), '23'),
    (nextval('hibernate_sequence'), '24'),
    (nextval('hibernate_sequence'), '25'),
    (nextval('hibernate_sequence'), '26'),
    (nextval('hibernate_sequence'), '27'),
    (nextval('hibernate_sequence'), '28'),
    (nextval('hibernate_sequence'), '29'),
    (nextval('hibernate_sequence'), '30'),
    (nextval('hibernate_sequence'), '31'),
    (nextval('hibernate_sequence'), '32'),
    (nextval('hibernate_sequence'), '33'),
    (nextval('hibernate_sequence'), '34'),
    (nextval('hibernate_sequence'), '35'),
    (nextval('hibernate_sequence'), '36'),
    (nextval('hibernate_sequence'), '37'),
    (nextval('hibernate_sequence'), '38');


ALTER TABLE ONLY peruste
    ADD CONSTRAINT fk_peruste_koulutusala FOREIGN KEY (koulutusala_id) REFERENCES koulutusala(id);

--UPDATE peruste p SET koulutusala_id = (SELECT id from koulutusala WHERE koodi = p.koulutusalakoodi) WHERE p.koulutusalakoodi is not NULL;