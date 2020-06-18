ALTER TABLE yl_oppiaineen_vlkok ADD COLUMN vapaaTeksti_id int8;

ALTER TABLE yl_oppiaineen_vlkok_AUD ADD COLUMN vapaaTeksti_id int8;

ALTER TABLE yl_oppiaineen_vlkok
        ADD CONSTRAINT FK_2spwiyd6bl1nfmhat9l78yhd0
        FOREIGN KEY (vapaaTeksti_id)
        REFERENCES tekstipalanen;

ALTER TABLE yl_aipe_oppiaine ADD COLUMN vapaaTeksti_id int8;

ALTER TABLE yl_aipe_oppiaine_AUD ADD COLUMN vapaaTeksti_id int8;

alter table yl_aipe_oppiaine
        add constraint FK_icgihw3xtwrksib0lao9j71j5
        foreign key (vapaaTeksti_id)
        references tekstipalanen;
