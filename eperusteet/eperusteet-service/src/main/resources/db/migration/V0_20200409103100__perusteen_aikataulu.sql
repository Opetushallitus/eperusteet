create table peruste_aikataulu (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tapahtuma varchar(255) not null,
    tapahtumapaiva timestamp not null,
    peruste_id int8,
    tavoite_id int8,
    primary key (id)
);

create table peruste_aikataulu_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    tapahtuma varchar(255),
    tapahtumapaiva timestamp,
    peruste_id int8,
    tavoite_id int8,
    primary key (id, REV)
);

alter table peruste_aikataulu
    add constraint FK_93cgs4uy64xjqqknqnjbo9wue
    foreign key (peruste_id)
    references peruste;

alter table peruste_aikataulu
    add constraint FK_8ubmb3b1vavw13tfctxjtk4u5
    foreign key (tavoite_id)
    references tekstipalanen;

alter table peruste_aikataulu_AUD
    add constraint FK_dg3qnjr4rma4br9nqcddu24ar
    foreign key (REV)
    references revinfo;

alter table peruste_aikataulu_AUD
    add constraint FK_soktrrqt17tiuoow0ml2553v5
    foreign key (REVEND)
    references revinfo;
