ALTER TABLE yl_laajaalainen_osaaminen ADD COLUMN muokattu TIMESTAMP;
ALTER TABLE yl_laajaalainen_osaaminen_aud ADD COLUMN muokattu TIMESTAMP;

create table aipevaihe_vapaateksti (
    aipevaihe_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    primary key (aipevaihe_id, kevyttekstikappaleet_order)
);

create table aipevaihe_vapaateksti_AUD (
    REV int4 not null,
    aipevaihe_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, aipevaihe_id, kevyttekstikappale_id, kevyttekstikappaleet_order)
);

alter table aipevaihe_vapaateksti
    add constraint FK_jcg2eb8lc873xadher3k4po0b
    foreign key (kevyttekstikappale_id)
    references kevyttekstikappale;

alter table aipevaihe_vapaateksti
    add constraint FK_9engptb0nt6p4wf4ff7gtvh7l
    foreign key (aipevaihe_id)
    references yl_aipe_vaihe;

alter table aipevaihe_vapaateksti_AUD
    add constraint FK_lhiqrks6wg5s13mh4y1hwv9vy
    foreign key (REV)
    references revinfo;

alter table aipevaihe_vapaateksti_AUD
    add constraint FK_5kr3jb2a3v92l4wtemu443syw
    foreign key (REVEND)
    references revinfo;

create table aipeoppiaine_vapaateksti (
    aipeoppiaine_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    primary key (aipeoppiaine_id, kevyttekstikappaleet_order)
);

create table aipeoppiaine_vapaateksti_AUD (
    REV int4 not null,
    aipeoppiaine_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, aipeoppiaine_id, kevyttekstikappale_id, kevyttekstikappaleet_order)
);

alter table aipeoppiaine_vapaateksti
    add constraint FK_4d3njvdq0y7voakg8p1iu6fnk
    foreign key (kevyttekstikappale_id)
    references kevyttekstikappale;

alter table aipeoppiaine_vapaateksti
    add constraint FK_q9hnod4yjap1g7mgwmrladcsw
    foreign key (aipeoppiaine_id)
    references yl_aipe_oppiaine;

alter table aipeoppiaine_vapaateksti_AUD
    add constraint FK_gn2fkcmksb53fg626s8rkfrhk
    foreign key (REV)
    references revinfo;

alter table aipeoppiaine_vapaateksti_AUD
    add constraint FK_2hycpal79mwx8i87g70g0n457
    foreign key (REVEND)
    references revinfo;