create table yl_vlkok_vuosiluokat (
        VuosiluokkaKokonaisuus_id int8 not null,
        vuosiluokka varchar(255)
    );

create table yl_vlkok_vuosiluokat_AUD (
    REV int4 not null,
    VuosiluokkaKokonaisuus_id int8 not null,
    vuosiluokka varchar(255) not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, VuosiluokkaKokonaisuus_id, vuosiluokka)
);

alter table yl_vlkok_vuosiluokat
    add constraint FK_rt84rt736i1bacjvsetwgya30
    foreign key (VuosiluokkaKokonaisuus_id)
    references yl_vlkokonaisuus;

alter table yl_vlkok_vuosiluokat_AUD
    add constraint FK_sp4jp99e2t7pgq5dsa94rqd9r
    foreign key (REV)
    references revinfo;

