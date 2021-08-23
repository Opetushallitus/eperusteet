create table maarays (
    id int8 not null,
    muokattu timestamp,
    muokkaaja varchar(255),
    nimi_id int8,
    primary key (id)
);

create table maarays_url (
    Maarays_id int8 not null,
    url varchar(255),
    url_KEY int4,
    primary key (Maarays_id, url_KEY)
);

alter table maarays
    add constraint FK_407ctd46a3ggsk5ravk23e0el
    foreign key (nimi_id)
    references tekstipalanen;

alter table maarays_url
    add constraint FK_qxrxy1rfivaih8ibyv3mt8i0o
    foreign key (Maarays_id)
    references maarays;
