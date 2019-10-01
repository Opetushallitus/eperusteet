create table skeduloitu_ajo (
        id int8 not null,
        nimi varchar(255) not null,
        viimeisinajo timestamp,
        primary key (id)
    );

alter table skeduloitu_ajo
        add constraint UK_ibu56vxb4nlaifyuuyoxg6r5k  unique (nimi);
