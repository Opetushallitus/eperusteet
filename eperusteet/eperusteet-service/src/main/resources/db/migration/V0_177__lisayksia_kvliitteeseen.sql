ALTER TABLE kvliite ADD COLUMN tutkinnostaPaattavaViranomainen TEXT;
ALTER TABLE kvliite ADD COLUMN tutkintotodistuksenSaaminen_id BIGINT REFERENCES tekstipalanen(id);

ALTER TABLE kvliite_aud ADD COLUMN tutkinnostaPaattavaViranomainen TEXT;
ALTER TABLE kvliite_aud ADD COLUMN tutkintotodistuksenSaaminen_id BIGINT;