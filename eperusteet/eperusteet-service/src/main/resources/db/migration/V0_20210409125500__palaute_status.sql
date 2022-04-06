create table palaute (
    stars int4 not null,
    key varchar(255) not null,
    createdAt timestamp not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    status varchar(255),
    primary key (stars, key, createdAt)
);

create table palaute_AUD (
    stars int4 not null,
    key varchar(255) not null,
    createdAt timestamp not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    status varchar(255),
    primary key (stars, key, createdAt, REV)
);

alter table palaute_AUD
    add constraint FK_qf3qi7s2r9hc2lb6l27dwymix
    foreign key (REV)
    references revinfo;

alter table palaute_AUD
    add constraint FK_g0n9y2lodl383ytvgkr7fxjlc
    foreign key (REVEND)
    references revinfo;

