ALTER TABLE yl_lukiokurssi ADD COLUMN sisalto_id INT8 REFERENCES yl_lukioopetuksen_perusteen_sisalto(id) NOT NULL DEFAULT NULL;
ALTER TABLE yl_lukiokurssi_aud ADD COLUMN sisalto_id INT8;
