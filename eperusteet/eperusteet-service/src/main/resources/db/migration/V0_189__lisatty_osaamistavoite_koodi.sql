ALTER TABLE osaamistavoite ADD COLUMN koodi_id BIGINT REFERENCES koodi(id);
ALTER TABLE osaamistavoite_aud ADD COLUMN koodi_id BIGINT;
