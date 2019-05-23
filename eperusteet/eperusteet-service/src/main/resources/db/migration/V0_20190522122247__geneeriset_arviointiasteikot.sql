create table geneerinen_arviointiasteikko (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    primary key (id)
);

create table geneerinen_arviointiasteikko_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    primary key (id, REV)
);

alter table geneerinen_arviointiasteikko 
    add constraint FK_g22gp10ufbng2lgua0n5ywxw 
    foreign key (nimi_id) 
    references tekstipalanen;

alter table geneerinen_arviointiasteikko_AUD 
    add constraint FK_bek75co0fbcvpddgh68sstuud 
    foreign key (REV) 
    references revinfo;

alter table geneerinen_arviointiasteikko_AUD 
    add constraint FK_oam7q3bx9496i9tp57ixvoonr 
    foreign key (REVEND) 
    references revinfo;
