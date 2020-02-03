create table Tiedote_julkaisupaikka (
    Tiedote_id int8 not null,
    julkaisupaikka varchar(255)
);

create table Tiedote_julkaisupaikka_AUD (
    REV int4 not null,
    Tiedote_id int8 not null,
    julkaisupaikka varchar(255) not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, Tiedote_id, julkaisupaikka)
);

create table Tiedote_koulutustyyppi (
    Tiedote_id int8 not null,
    koulutustyyppi varchar(255)
);

create table Tiedote_koulutustyyppi_AUD (
    REV int4 not null,
    Tiedote_id int8 not null,
    koulutustyyppi varchar(255) not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, Tiedote_id, koulutustyyppi)
);

create table tiedote_peruste (
    tiedote_id int8 not null,
    peruste_id int8 not null,
    primary key (tiedote_id, peruste_id)
);

create table tiedote_peruste_AUD (
    REV int4 not null,
    tiedote_id int8 not null,
    peruste_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tiedote_id, peruste_id)
);

alter table Tiedote_julkaisupaikka
    add constraint FK_521k5iapvitsbh9ksgypnwch4
    foreign key (Tiedote_id)
    references tiedote;

alter table Tiedote_julkaisupaikka_AUD
    add constraint FK_dv00mm6purd5e8yw9o7va9gv2
    foreign key (REV)
    references revinfo;

alter table Tiedote_julkaisupaikka_AUD
    add constraint FK_p0rp3846c321bqyro5r5fiu0w
    foreign key (REVEND)
    references revinfo;

alter table Tiedote_koulutustyyppi
    add constraint FK_ah54ird5chx5hh6d90qqrxr9a
    foreign key (Tiedote_id)
    references tiedote;

alter table Tiedote_koulutustyyppi_AUD
    add constraint FK_231iiadp11twh15lmhcu649yx
    foreign key (REV)
    references revinfo;

alter table Tiedote_koulutustyyppi_AUD
    add constraint FK_bx9422rmxrne07j6bahdxpmb1
    foreign key (REVEND)
    references revinfo;

alter table tiedote_peruste
    add constraint FK_gqvca6xnkko4a79743smv19lx
    foreign key (peruste_id)
    references peruste;

alter table tiedote_peruste
    add constraint FK_9n5qgqgor9wolnsrl7cv78p4v
    foreign key (tiedote_id)
    references tiedote;

alter table tiedote_peruste_AUD
    add constraint FK_jehddt4iysaatohly806284ae
    foreign key (REV)
    references revinfo;

alter table tiedote_peruste_AUD
    add constraint FK_1mjiixm05meay1at2sor6fso
    foreign key (REVEND)
    references revinfo;
