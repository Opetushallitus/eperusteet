ALTER TABLE tutkinnonosa ADD COLUMN geneerinenArviointiasteikko_id BIGINT REFERENCES geneerinenarviointiasteikko(id);
ALTER TABLE tutkinnonosa_aud ADD COLUMN geneerinenArviointiasteikko_id BIGINT REFERENCES geneerinenarviointiasteikko(id);
