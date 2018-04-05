ALTER TABLE tutkinnon_rakenne ADD COLUMN tutkintonimike_id BIGINT REFERENCES koodi(id);
ALTER TABLE tutkinnon_rakenne_aud ADD COLUMN tutkintonimike_id BIGINT REFERENCES koodi(id);