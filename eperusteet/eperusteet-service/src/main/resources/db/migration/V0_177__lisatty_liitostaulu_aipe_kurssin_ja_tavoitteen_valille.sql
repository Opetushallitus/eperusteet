create table yl_aipe_kurssi_yl_opetuksen_tavoite (
    yl_aipe_kurssi_id int8 not null,
    tavoitteet_id int8 not null,
    primary key (yl_aipe_kurssi_id, tavoitteet_id)
);

create table yl_aipe_kurssi_yl_opetuksen_tavoite_AUD (
    REV int4 not null,
    yl_aipe_kurssi_id int8 not null,
    tavoitteet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, yl_aipe_kurssi_id, tavoitteet_id)
);

alter table yl_aipe_kurssi_yl_opetuksen_tavoite
    add constraint FK_ln5abwcgj5hk3rk4gnlo88mnb
    foreign key (tavoitteet_id)
    references yl_opetuksen_tavoite;

alter table yl_aipe_kurssi_yl_opetuksen_tavoite
    add constraint FK_rkyobpu1pag28bbuq1h0a02vf
    foreign key (yl_aipe_kurssi_id)
    references public.yl_aipe_kurssi;

alter table yl_aipe_kurssi_yl_opetuksen_tavoite_AUD
    add constraint FK_dhgudrhp0v2x8uc9tw8m3ywgw
    foreign key (REV)
    references revinfo;

alter table yl_aipe_kurssi_yl_opetuksen_tavoite_AUD
    add constraint FK_bndvus3uqhsxgegn996xavrk0
    foreign key (REVEND)
    references revinfo;
