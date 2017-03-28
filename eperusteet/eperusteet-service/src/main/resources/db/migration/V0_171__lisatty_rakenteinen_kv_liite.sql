create table kvliite (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tutkintotodistuksenAntaja varchar(255),
    arvosanaAsteikko_id int8,
    tutkinnonVirallinenAsema_id int8,
    jatkoopintoKelpoisuus_id int8,
    kansainvalisetSopimukset_id int8,
    lisatietoja_id int8,
    peruste_id int8 not null,
    pohja_id int8,
    pohjakoulutusvaatimukset_id int8,
    saadosPerusta_id int8,
    suorittaneenOsaaminen_id int8,
    tyotehtavatJoissaVoiToimia_id int8,
    primary key (id)
);

create table kvliite_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tutkintotodistuksenAntaja varchar(255),
    arvosanaAsteikko_id int8,
    jatkoopintoKelpoisuus_id int8,
    kansainvalisetSopimukset_id int8,
    tutkinnonVirallinenAsema_id int8,
    lisatietoja_id int8,
    peruste_id int8,
    pohja_id int8,
    pohjakoulutusvaatimukset_id int8,
    saadosPerusta_id int8,
    suorittaneenOsaaminen_id int8,
    tyotehtavatJoissaVoiToimia_id int8,
    primary key (id, REV)
);


alter table kvliite
    add constraint FK_lptl2fqboul4q558n7h2b5tjf
    foreign key (arvosanaAsteikko_id)
    references tekstipalanen;

alter table kvliite
    add constraint FK_e7qqw9n8u73rijxf251h6ficu
    foreign key (jatkoopintoKelpoisuus_id)
    references tekstipalanen;

alter table kvliite
    add constraint FK_p6jmm1i19r4f112awyleboni5
    foreign key (kansainvalisetSopimukset_id)
    references tekstipalanen;

alter table kvliite
    add constraint FK_krjaps1bae04y4kxdgi83nlhe
    foreign key (lisatietoja_id)
    references tekstipalanen;

alter table kvliite
    add constraint FK_41e3j9t6cqalnrjpfvdt941tx
    foreign key (peruste_id)
    references peruste;

alter table kvliite
    add constraint FK_ojpkj4atts8l3qnnayxdfj32v
    foreign key (pohjakoulutusvaatimukset_id)
    references tekstipalanen;

alter table kvliite
    add constraint FK_bh4bpjr3wiemqqt3oj31904l
    foreign key (saadosPerusta_id)
    references tekstipalanen;

alter table kvliite
    add constraint FK_1bdmtglr553tafqtl4tmaxlp9
    foreign key (suorittaneenOsaaminen_id)
    references tekstipalanen;

alter table kvliite
    add constraint FK_c76ehh4exmwx441bn1hm013my
    foreign key (tyotehtavatJoissaVoiToimia_id)
    references tekstipalanen;

alter table kvliite_AUD
    add constraint FK_5xpwdlqtp0m01s53ns5la1rex
    foreign key (REV)
    references revinfo;

alter table kvliite_AUD
    add constraint FK_rkqurgyvmruebgbun0iy43j3n
    foreign key (REVEND)
    references revinfo;

alter table kvliite
    add constraint FK_bi4uxvv6hwb3yx4v8rn5sy6ji
    foreign key (tutkinnonVirallinenAsema_id)
    references tekstipalanen;

alter table kvliite
    add constraint FK_phtkb3tg6teitboo12t18c2w
    foreign key (pohja_id)
    references kvliite;