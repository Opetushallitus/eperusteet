create table opas_kiinnitetty_koodi (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kiinnitettyKoodiTyyppi varchar(255) not null,
    koodi_id int8,
    opasSisalto_id int8 not null,
    primary key (id)
);

create table opas_kiinnitetty_koodi_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kiinnitettyKoodiTyyppi varchar(255),
    koodi_id int8,
    opasSisalto_id int8,
    primary key (id, REV)
);

alter table opas_kiinnitetty_koodi
    add constraint FK_rghw3vkqoexdyuibduhp61gmw
    foreign key (koodi_id)
    references koodi;

alter table opas_kiinnitetty_koodi
    add constraint FK_s0upbo1x1so60nqnofpdc8hvw
    foreign key (opasSisalto_id)
    references opas_sisalto;

alter table opas_kiinnitetty_koodi_AUD
    add constraint FK_7552ov7u0h7r3buv7n5eikac1
    foreign key (REV)
    references revinfo;

alter table opas_kiinnitetty_koodi_AUD
    add constraint FK_er1f0e3ruyulq5tql0voesfsj
    foreign key (REVEND)
    references revinfo;
