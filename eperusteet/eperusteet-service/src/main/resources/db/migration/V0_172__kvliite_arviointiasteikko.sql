DELETE FROM kvliite *;
DELETE FROM kvliite_aud *;

ALTER TABLE kvliite DROP COLUMN arvosanaasteikko_id;
ALTER TABLE kvliite_aud DROP COLUMN arvosanaasteikko_id;

ALTER TABLE kvliite ADD COLUMN arvosanaasteikko_id int8 REFERENCES arviointiasteikko(id);
ALTER TABLE kvliite_aud ADD COLUMN arvosanaasteikko_id int8;