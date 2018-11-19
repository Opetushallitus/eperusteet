CREATE SEQUENCE amattitaitovaatimus_koodi_arvo_seq START 1;

CREATE TABLE amattitaitovaatimus_koodi (
    id BIGINT NOT NULL,
    arvo BIGINT UNIQUE DEFAULT nextval('amattitaitovaatimus_koodi_arvo_seq'),
    PRIMARY KEY (id)
);

ALTER TABLE arvioinninkohdealue ADD COLUMN koodi_id BIGINT REFERENCES amattitaitovaatimus_koodi(id);
ALTER TABLE arvioinninkohdealue_aud ADD COLUMN koodi_id BIGINT;

UPDATE arvioinninkohdealue_aud
    SET koodi_id = (
        SELECT arvioinninkohdealue.koodi_id
        FROM arvioinninkohdealue
        WHERE arvioinninkohdealue.id = arvioinninkohdealue_aud.id);

INSERT INTO amattitaitovaatimus_koodi (id)
    (SELECT aka.koodi_id FROM arvioinninkohdealue AS aka);

ALTER TABLE amattitaitovaatimus_koodi ALTER COLUMN arvo SET NOT NULL;