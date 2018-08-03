create table koulutuskoodi_status (
    id int8 not null,
    kooditOk boolean not null,
    aikaleima timestamp not null,
    peruste_id int8 not null,
    primary key (id)
);

create table koulutuskoodi_status_info (
    id int8 not null,
    suoritustapa varchar(255),
    viite_id int8,
    primary key (id)
);

create table koulutuskoodi_status_koulutuskoodi_status_info (
    koulutuskoodi_status_id int8 not null,
    infot_id int8 not null
);

alter table koulutuskoodi_status
        add constraint UK_r975k7uystrq7cmf37iu8s7ao  unique (peruste_id);

alter table koulutuskoodi_status_koulutuskoodi_status_info
        add constraint UK_ej47dqjh9e9f3jvyhqj0gkog5  unique (infot_id);

alter table koulutuskoodi_status
        add constraint FK_r975k7uystrq7cmf37iu8s7ao
        foreign key (peruste_id)
        references peruste;

alter table koulutuskoodi_status_info
        add constraint FK_bwsifjkoouud06tacdubt1dwj
        foreign key (viite_id)
        references tutkinnonosaviite;

alter table koulutuskoodi_status_koulutuskoodi_status_info
        add constraint FK_ej47dqjh9e9f3jvyhqj0gkog5
        foreign key (infot_id)
        references koulutuskoodi_status_info;

alter table koulutuskoodi_status_koulutuskoodi_status_info
        add constraint FK_mjp6mh521juimjfkn1s8k8e6t
        foreign key (koulutuskoodi_status_id)
        references koulutuskoodi_status;