ALTER TABLE tutkinnon_rakenne ADD COLUMN tunniste uuid UNIQUE;
ALTER TABLE tutkinnon_rakenne_aud ADD COLUMN tunniste uuid;