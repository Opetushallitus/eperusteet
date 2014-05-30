CREATE TABLE kommentti (
    muokkaaja varchar not null,
    luoja varchar not null,
    id bigint not null primary key,
    parent_id bigint references kommentti(id),
    ylin_id bigint references kommentti(id),
    luotu timestamp with time zone not null,
    muokattu timestamp with time zone,
    sisalto varchar not null,
    poistettu boolean default false,
    perusteprojekti_id bigint not null references perusteprojekti(id)
);