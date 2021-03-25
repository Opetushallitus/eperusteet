create table tiedote_osaamisalat (
    tiedote_id int8 not null,
    osaamisala_id int8 not null,
    primary key (tiedote_id, osaamisala_id)
);

create table tiedote_osaamisalat_AUD (
    REV int4 not null,
    tiedote_id int8 not null,
    osaamisala_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tiedote_id, osaamisala_id)
);

create table tiedote_tutkinnonosat (
    tiedote_id int8 not null,
    tutkinnonosa_id int8 not null,
    primary key (tiedote_id, tutkinnonosa_id)
);

create table tiedote_tutkinnonosat_AUD (
    REV int4 not null,
    tiedote_id int8 not null,
    tutkinnonosa_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tiedote_id, tutkinnonosa_id)
);

alter table tiedote_osaamisalat
    add constraint FK_p2wixchva9ooatwr1xvx1hxb9
    foreign key (osaamisala_id)
    references koodi;

alter table tiedote_osaamisalat
    add constraint FK_lwd0ng2isijktkjbw85agv3rq
    foreign key (tiedote_id)
    references tiedote;

alter table tiedote_osaamisalat_AUD
    add constraint FK_7npsws4d09ietdjqli1h2d7o7
    foreign key (REV)
    references revinfo;

alter table tiedote_osaamisalat_AUD
    add constraint FK_lhx2idvxeuiqj8k7j964m1d2k
    foreign key (REVEND)
    references revinfo;

alter table tiedote_tutkinnonosat
    add constraint FK_gjt9d2dbpuqekm7ysrc4n6sam
    foreign key (tutkinnonosa_id)
    references koodi;

alter table tiedote_tutkinnonosat
    add constraint FK_hxch8xqhy2y678hj1e5v7irmb
    foreign key (tiedote_id)
    references tiedote;

alter table tiedote_tutkinnonosat_AUD
    add constraint FK_d8y4yl5tg8yau4qjmxsg6l1b6
    foreign key (REV)
    references revinfo;

alter table tiedote_tutkinnonosat_AUD
    add constraint FK_photqve8exhctvsrncl0f004t
    foreign key (REVEND)
    references revinfo;
