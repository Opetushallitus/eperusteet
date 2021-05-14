alter table yl_lops2019_oppiaine add column opiskeluymparisto_tyotavat_id int8;

alter table yl_lops2019_oppiaine_aud add column opiskeluymparisto_tyotavat_id int8;

create table yl_lops2019_oppiaine_opiskeluymparisto_tyotavat (
    id int8 not null,
    kuvaus_id int8,
    primary key (id)
);

create table yl_lops2019_oppiaine_opiskeluymparisto_tyotavat_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kuvaus_id int8,
    primary key (id, REV)
);

alter table yl_lops2019_oppiaine
    add constraint FK_r6lnwc8bxm6wfbkp48hk1bqkf
    foreign key (opiskeluymparisto_tyotavat_id)
    references yl_lops2019_oppiaine_opiskeluymparisto_tyotavat;

alter table yl_lops2019_oppiaine_opiskeluymparisto_tyotavat
    add constraint FK_iid1j4t0ll8q9q50a6vc7cox
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table yl_lops2019_oppiaine_opiskeluymparisto_tyotavat_AUD
    add constraint FK_dl5m3v2dgiwy76w4k9chmudkg
    foreign key (REV)
    references revinfo;

alter table yl_lops2019_oppiaine_opiskeluymparisto_tyotavat_AUD
    add constraint FK_fjsy92b3a6u3e64d1tj0miv7g
    foreign key (REVEND)
    references revinfo;
