drop table if exists julkaistu_peruste_data cascade;
drop table if exists julkaistu_peruste cascade;

create table julkaistu_peruste (
    id int8 not null,
    luoja varchar(255) not null,
    luotu timestamp,
    revision int4 not null,
    data_id int8 not null,
    peruste_id int8 not null,
    tiedote_id int8 not null,
    primary key (id)
);

create table julkaistu_peruste_data (
    id int8 not null,
    hash int4 not null,
    data jsonb not null,
    primary key (id)
);

alter table julkaistu_peruste 
    add constraint FK_96x9ob5stusy9tujn4tx9gr7k 
    foreign key (data_id) 
    references julkaistu_peruste_data;

alter table julkaistu_peruste 
    add constraint FK_p3rbyqvky1jewdvtckmyjlin2 
    foreign key (peruste_id) 
    references peruste;

alter table julkaistu_peruste 
    add constraint FK_9pykadqq9ugpb1qvbg4af055o 
    foreign key (tiedote_id) 
    references tekstipalanen;
