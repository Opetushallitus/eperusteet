-- tutkintotodistuksenAntaja
ALTER TABLE kvliite ADD COLUMN tutkintotodistuksenAntaja_id BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE kvliite_aud ADD COLUMN tutkintotodistuksenAntaja_id BIGINT;

-- tutkinnostaPaattavaViranomainen
ALTER TABLE kvliite ADD COLUMN tutkinnostaPaattavaViranomainen_id BIGINT REFERENCES tekstipalanen(id);
ALTER TABLE kvliite_aud ADD COLUMN tutkinnostaPaattavaViranomainen_id BIGINT;
