ALTER TABLE arvioinninkohdealue ADD COLUMN koodi_id BIGINT REFERENCES koodi (id);

ALTER TABLE arvioinninkohdealue_aud ADD COLUMN koodi_id BIGINT;