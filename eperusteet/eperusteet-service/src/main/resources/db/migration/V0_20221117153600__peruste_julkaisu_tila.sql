create table julkaistu_peruste_tila (
    peruste_id int8 not null,
    julkaisu_tila varchar(255) not null,
    muokattu timestamp not null,
    primary key (peruste_id)
);