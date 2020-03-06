create table perusteen_muokkaustieto (
    id int8 not null,
    kohde varchar(255) not null,
    kohde_id int8 not null,
    lisatieto varchar(255),
    luotu timestamp,
    muokkaaja varchar(255),
    peruste_id int8,
    poistettu boolean not null,
    tapahtuma varchar(255) not null,
    nimi_id int8,
    primary key (id)
);

alter table perusteen_muokkaustieto
    add constraint FK_ruj7aer9lvr5dfgcma68wfep5
    foreign key (nimi_id)
    references tekstipalanen;
