ALTER TABLE tutkinnonosa_osaalue ADD COLUMN kielikoodi_id BIGINT;
ALTER TABLE tutkinnonosa_osaalue_aud ADD COLUMN kielikoodi_id BIGINT;

alter table tutkinnonosa_osaalue
    add constraint FK_a527d3vgcmt811hfsput6j9x4
    foreign key (kielikoodi_id)
    references koodi;

ALTER TABLE tutkinnonosa_osaalue DROP COLUMN kieli;
ALTER TABLE tutkinnonosa_osaalue_aud DROP COLUMN kieli;
