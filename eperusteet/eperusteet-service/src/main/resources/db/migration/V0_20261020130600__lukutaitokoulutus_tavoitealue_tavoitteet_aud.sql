create table lukutaitokoulutus_tavoitealue_tavoitteet_AUD (
    REV int4 not null,
    tavoitealue_id int8 not null,
    tavoite_id int8 not null,
    tavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tavoitealue_id, tavoite_id, tavoitteet_ORDER)
);

alter table lukutaitokoulutus_tavoitealue_tavoitteet_AUD
    add constraint FK_lukutaitokoulutus_tavoitealue_tavoitteet_AUD_REV
    foreign key (REV)
    references revinfo;

alter table lukutaitokoulutus_tavoitealue_tavoitteet_AUD
    add constraint FK_lukutaitokoulutus_tavoitealue_tavoitteet_AUD_REVEND
    foreign key (REVEND)
    references revinfo;
