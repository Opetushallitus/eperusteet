create table koto_laaja_alainen_osaaminen
(
    id             bigint not null,
    yleiskuvaus_id bigint,
    primary key (id)
);

create table koto_laaja_alainen_osaaminen_AUD
(
    id             bigint  not null,
    REV            integer not null,
    yleiskuvaus_id bigint,
    primary key (id, REV)
);

alter table koto_laaja_alainen_osaaminen
    add constraint FK_hok2qveyvb898xplau79sios5
        foreign key (yleiskuvaus_id)
            references tekstipalanen;

alter table koto_laaja_alainen_osaaminen
    add constraint FK_7shbjr7pkoqafenitkxvkw4ft
        foreign key (id)
            references perusteenosa;

alter table koto_laaja_alainen_osaaminen_AUD
    add constraint FK_frv5awq7r3e9xocnpnu8fi0m8
        foreign key (id, REV)
            references perusteenosa_AUD;