alter table osaamiskokonaisuus add column keskeinenKasitteisto_id int8;
alter table osaamiskokonaisuus_AUD add column keskeinenKasitteisto_id int8;

alter table osaamiskokonaisuus_kasitteisto drop column keskeinenKasitteisto_id;
alter table osaamiskokonaisuus_kasitteisto_aud drop column keskeinenKasitteisto_id;

alter table osaamiskokonaisuus
    add constraint FK_elqwct8hi1uvwt0rj00r692xx
    foreign key (keskeinenKasitteisto_id)
    references tekstipalanen;