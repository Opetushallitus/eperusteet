ALTER TABLE yl_lukiokurssi ADD COLUMN tavoitteet_otsikko_id INT8 REFERENCES tekstipalanen(id);
ALTER TABLE yl_lukiokurssi_aud ADD COLUMN tavoitteet_otsikko_id INT8;
