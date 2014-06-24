create table kommentti (
    muokkaaja varchar not null,
    luoja varchar not null,
    id bigint not null primary key,
    parent_id bigint default 0,
    ylin_id bigint default 0,
    luotu timestamp with time zone not null,
    muokattu timestamp with time zone,
    sisalto varchar not null,
    poistettu boolean default false,
    perusteprojekti_id bigint not null references perusteprojekti(id),
    viite_suoritustapa character varying(255),
    viite_perusteenosa_id bigint default 0
);