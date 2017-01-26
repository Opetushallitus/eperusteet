create table yl_aipe_opetuksensisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8 not null,
    sisalto_id int8,
    primary key (id)
);

create table yl_aipe_opetuksensisalto_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    peruste_id int8,
    sisalto_id int8,
    primary key (id, REV)
);

create table yl_aipe_opetuksensisalto_yl_aipe_vaihe (
    yl_aipe_opetuksensisalto_id int8 not null,
    vaiheet_id int8 not null,
    primary key (yl_aipe_opetuksensisalto_id, vaiheet_id)
);

create table yl_aipe_opetuksensisalto_yl_aipe_vaihe_AUD (
    REV int4 not null,
    yl_aipe_opetuksensisalto_id int8 not null,
    vaiheet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, yl_aipe_opetuksensisalto_id, vaiheet_id)
);

create table yl_aipe_vaihe (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    primary key (id)
);

create table yl_aipe_vaihe_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (id, REV)
);

alter table yl_aipe_opetuksensisalto
    add constraint UK_4nqcxldr6s0h4ri5reoeb7u91  unique (peruste_id);

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe
    add constraint UK_2b125utu3e10xhojc3c764pyj  unique (vaiheet_id);

alter table yl_aipe_opetuksensisalto
    add constraint FK_4nqcxldr6s0h4ri5reoeb7u91
    foreign key (peruste_id)
    references peruste;

alter table yl_aipe_opetuksensisalto
    add constraint FK_1thio2jlde4hwbixq4e3q6akt
    foreign key (sisalto_id)
    references perusteenosaviite;

alter table yl_aipe_opetuksensisalto_AUD
    add constraint FK_ag14gtw71fd0o21g90gics7pp
    foreign key (REV)
    references revinfo;

alter table yl_aipe_opetuksensisalto_AUD
    add constraint FK_1ygpt4m4el35pho92k7nxu0hp
    foreign key (REVEND)
    references revinfo;

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe
    add constraint FK_2b125utu3e10xhojc3c764pyj
    foreign key (vaiheet_id)
    references yl_aipe_vaihe;

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe
    add constraint FK_fq70oywteyoxoks7k3sfsjs8f
    foreign key (yl_aipe_opetuksensisalto_id)
    references yl_aipe_opetuksensisalto;

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe_AUD
    add constraint FK_9rmxxlb0grd9u146ovbiq0s32
    foreign key (REV)
    references revinfo;

alter table yl_aipe_opetuksensisalto_yl_aipe_vaihe_AUD
    add constraint FK_hq5rs4b45kg9ppwoi9ppovrgc
    foreign key (REVEND)
    references revinfo;

alter table yl_aipe_vaihe_AUD
    add constraint FK_rajwj5tu06oc8pb0chvnvrahs
    foreign key (REV)
    references revinfo;

alter table yl_aipe_vaihe_AUD
    add constraint FK_qxe806d1krhtmxgs4q9e26ine
    foreign key (REVEND)
    references revinfo;
