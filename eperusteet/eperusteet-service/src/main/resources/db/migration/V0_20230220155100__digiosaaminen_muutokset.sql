drop table osaamiskokonaisuus_osa_alue_tasokuvaus_kuvaus_join;
drop table osaamiskokonaisuus_osa_alue_tasokuvaus_kuvaus_join_aud;

create table osaamiskokonaisuus_osa_alue_tasokuvaus_ed_keh_osaa_join (
    osaamiskokonaisuus_osa_alue_tasokuvaus_id int8 not null,
    edelleenKehittyvatOsaamiset_id int8 not null,
    edelleenKehittyvatOsaamiset_ORDER int4 not null,
    primary key (osaamiskokonaisuus_osa_alue_tasokuvaus_id, edelleenKehittyvatOsaamiset_ORDER)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus_ed_keh_osaa_join_AUD (
    REV int4 not null,
    osaamiskokonaisuus_osa_alue_tasokuvaus_id int8 not null,
    edelleenKehittyvatOsaamiset_id int8 not null,
    edelleenKehittyvatOsaamiset_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaamiskokonaisuus_osa_alue_tasokuvaus_id, edelleenKehittyvatOsaamiset_id, edelleenKehittyvatOsaamiset_ORDER)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus_osaamiset_join (
    osaamiskokonaisuus_osa_alue_tasokuvaus_id int8 not null,
    osaamiset_id int8 not null,
    osaamiset_ORDER int4 not null,
    primary key (osaamiskokonaisuus_osa_alue_tasokuvaus_id, osaamiset_ORDER)
);

create table osaamiskokonaisuus_osa_alue_tasokuvaus_osaamiset_join_AUD (
    REV int4 not null,
    osaamiskokonaisuus_osa_alue_tasokuvaus_id int8 not null,
    osaamiset_id int8 not null,
    osaamiset_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, osaamiskokonaisuus_osa_alue_tasokuvaus_id, osaamiset_id, osaamiset_ORDER)
);

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_ed_keh_osaa_join
    add constraint FK_oryoapcbq5gwohbsgpnjyprih
    foreign key (edelleenKehittyvatOsaamiset_id)
    references tekstipalanen;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_ed_keh_osaa_join
    add constraint FK_bdx7ul5p722d8aiftaofh11sa
    foreign key (osaamiskokonaisuus_osa_alue_tasokuvaus_id)
    references osaamiskokonaisuus_osa_alue_tasokuvaus;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_ed_keh_osaa_join_AUD
    add constraint FK_91e52qmiytanws3f3iqmaj6wt
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_ed_keh_osaa_join_AUD
    add constraint FK_s55v4dc4xm75k7ch6fhjyagcb
    foreign key (REVEND)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_osaamiset_join
    add constraint FK_7qfygi6hstun633exqe7nkud0
    foreign key (osaamiset_id)
    references tekstipalanen;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_osaamiset_join
    add constraint FK_26au20unqaxxu3liq86gjk9id
    foreign key (osaamiskokonaisuus_osa_alue_tasokuvaus_id)
    references osaamiskokonaisuus_osa_alue_tasokuvaus;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_osaamiset_join_AUD
    add constraint FK_bm30qmkg35cf3erwoecb7b6uf
    foreign key (REV)
    references revinfo;

alter table osaamiskokonaisuus_osa_alue_tasokuvaus_osaamiset_join_AUD
    add constraint FK_99vs7td5mjifpd1mu0flwvdge
    foreign key (REVEND)
    references revinfo;