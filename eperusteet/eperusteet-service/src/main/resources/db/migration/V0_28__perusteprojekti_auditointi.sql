alter table perusteprojekti
    add luoja varchar(255),
    add luotu timestamp,
    add muokattu timestamp,
    add muokkaaja varchar(255);

create table perusteprojekti_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    diaarinumero varchar(255),
    nimi varchar(255),
    paatosPvm timestamp,
    tehtava varchar(255),
    tehtavaluokka varchar(255),
    toimikausi_alku timestamp,
    toimikausi_loppu timestamp,
    yhteistyotaho varchar(255),
    peruste_id int8,
    primary key (id, REV)
);

alter table perusteprojekti_AUD
     add constraint FK_perusteprojekti_AUD_REV_REVINFO
     foreign key (REV)
     references REVINFO;

 alter table perusteprojekti_AUD
     add constraint FK_FK_perusteprojekti_AUD_REVEND_REVINFO
     foreign key (REVEND)
     references REVINFO;
