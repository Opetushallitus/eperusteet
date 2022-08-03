create table poistettu_sisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    poistettu_id int8,
    tyyppi varchar(255) not null,
    nimi_id int8,
    peruste_id int8 not null,
    primary key (id)
);

create table poistettu_sisalto_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    poistettu_id int8,
    tyyppi varchar(255),
    nimi_id int8,
    peruste_id int8,
    primary key (id, REV)
);

alter table poistettu_sisalto
    add constraint FK_3k8m3xisk82gvxpvfej887bg6
    foreign key (nimi_id)
    references tekstipalanen;

alter table poistettu_sisalto
    add constraint FK_5143x9xx64or75ra7ex8otq6t
    foreign key (peruste_id)
    references peruste;

alter table poistettu_sisalto_AUD
    add constraint FK_9hra1q5g23ntwijthweuktsj4
    foreign key (REV)
    references revinfo;

alter table poistettu_sisalto_AUD
    add constraint FK_54iysgcmqs0hknwww0ul3nce4
    foreign key (REVEND)
    references revinfo;
