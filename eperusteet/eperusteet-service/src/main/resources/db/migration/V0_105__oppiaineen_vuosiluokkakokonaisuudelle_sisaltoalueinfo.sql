ALTER TABLE yl_oppiaineen_vlkok
    ADD COLUMN sisaltoalueinfo_id bigint REFERENCES yl_tekstiosa(id);

ALTER TABLE yl_oppiaineen_vlkok_aud
    ADD COLUMN sisaltoalueinfo_id bigint;
