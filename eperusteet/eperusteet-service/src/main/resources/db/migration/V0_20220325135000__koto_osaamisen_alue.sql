create table koto_laaja_alainen_osaaminen_alue
(
    id        bigint       not null,
    luoja     varchar(255),
    luotu     timestamp,
    muokattu  timestamp,
    muokkaaja varchar(255),
    koodi_id  bigint,
    kuvaus_id bigint,
    primary key (id)
);

create table koto_laaja_alainen_osaaminen_alue_AUD
(
    id        int8 not null,
    REV       int4 not null,
    REVTYPE   int2,
    REVEND    int4,
    luoja     varchar(255),
    luotu     timestamp,
    muokattu  timestamp,
    muokkaaja varchar(255),
    koodi_id  int8,
    kuvaus_id int8,
    primary key (id, REV)
);

create table koto_laaja_alainen_osaaminen_osaamisen_alue
(
    koto_laaja_alainen_osaaminen_id int8 not null,
    osaamisalue_id                  int8 not null,
    osaamisAlueet_ORDER             int4 not null,
    primary key (koto_laaja_alainen_osaaminen_id, osaamisAlueet_ORDER)
);

create table koto_laaja_alainen_osaaminen_osaamisen_alue_AUD
(
    REV                             int4 not null,
    koto_laaja_alainen_osaaminen_id int8 not null,
    osaamisalue_id                  int8 not null,
    osaamisAlueet_ORDER             int4 not null,
    REVTYPE                         int2,
    REVEND                          int4,
    primary key (REV, koto_laaja_alainen_osaaminen_id, osaamisalue_id, osaamisAlueet_ORDER)
);

alter table koto_laaja_alainen_osaaminen_osaamisen_alue
    add constraint UK_konlj2yguhmetsdkug0l9lkr7 unique (osaamisalue_id);

alter table koto_laaja_alainen_osaaminen_alue
    add constraint FK_nmw9w83houx7y6rf6xjeu7c6w
        foreign key (koodi_id)
            references koodi;

alter table koto_laaja_alainen_osaaminen_alue
    add constraint FK_ovd93m50c71j4j1lwhf61t8ps
        foreign key (kuvaus_id)
            references tekstipalanen;

alter table koto_laaja_alainen_osaaminen_alue_AUD
    add constraint FK_qncq2cuf2ihnle5s1vp3jj7q1
        foreign key (REV)
            references revinfo;

alter table koto_laaja_alainen_osaaminen_alue_AUD
    add constraint FK_ric6k1embqmorf21tl0mdu9ep
        foreign key (REVEND)
            references revinfo;

alter table koto_laaja_alainen_osaaminen_osaamisen_alue
    add constraint FK_konlj2yguhmetsdkug0l9lkr7
        foreign key (osaamisalue_id)
            references koto_laaja_alainen_osaaminen_alue;

alter table koto_laaja_alainen_osaaminen_osaamisen_alue
    add constraint FK_4i953p2fb5qrsdkv8buf3ylf3
        foreign key (koto_laaja_alainen_osaaminen_id)
            references koto_laaja_alainen_osaaminen;

alter table koto_laaja_alainen_osaaminen_osaamisen_alue_AUD
    add constraint FK_4j4df0y96nhbudfkv6yybju1c
        foreign key (REV)
            references revinfo;

alter table koto_laaja_alainen_osaaminen_osaamisen_alue_AUD
    add constraint FK_253r5mdscbobkyi330xq9qwr1
        foreign key (REVEND)
            references revinfo;

