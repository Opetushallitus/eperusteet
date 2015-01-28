alter table yl_keskeinen_sisaltoalue
    add column tunniste uuid;

alter table yl_keskeinen_sisaltoalue_AUD
    add column tunniste uuid;

alter table yl_vlkokonaisuus
    add column tunniste uuid;

alter table yl_vlkokonaisuus_AUD
    add column tunniste uuid;

alter table yl_laajaalainen_osaaminen
    add column tunniste uuid;

alter table yl_laajaalainen_osaaminen_AUD
    add column tunniste uuid;

alter table yl_opetuksen_tavoite
    add column tunniste uuid;

alter table yl_opetuksen_tavoite_AUD
    add column tunniste uuid;
