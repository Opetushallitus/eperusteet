create table JulkaistuPeruste_dokumentit (
        JulkaistuPeruste_id int8 not null,
        dokumentit int8
    );

alter table JulkaistuPeruste_dokumentit
        add constraint FK_9hpoo8hdvbrtsk8lg0mj6oqlt
        foreign key (JulkaistuPeruste_id)
        references julkaistu_peruste;
