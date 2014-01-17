    create table arviointi (
        id int8 not null,
        lisatiedot_id int8,
        primary key (id)
    );

    create table arviointi_kohdealue (
        arviointi_id int8 not null,
        kohdealue_id int8 not null,
        kohdealueet_ORDER int4 not null,
        primary key (arviointi_id, kohdealueet_ORDER)
    );

    create table arviointiasteikko (
        id int8 not null,
        primary key (id)
    );

    create table arviointiasteikko_osaamistaso (
        arviointiasteikko_id int8 not null,
        osaamistasot_id int8 not null,
        osaamistasot_ORDER int4 not null,
        primary key (arviointiasteikko_id, osaamistasot_ORDER)
    );

    create table kohde (
        id int8 not null,
        Arviointiasteikko_id int8,
        otsikko_id int8,
        primary key (id)
    );

    create table kohde_kriteeri (
        kohde_id int8 not null,
        kriteerit_id int8 not null
    );

    create table kohdealue (
        id int8 not null,
        otsikko_id int8,
        primary key (id)
    );

    create table kohdealue_kohde (
        kohdealue_id int8 not null,
        kohde_id int8 not null,
        kohteet_ORDER int4 not null,
        primary key (kohdealue_id, kohteet_ORDER)
    );

    create table kriteeri (
        id int8 not null,
        Osaamistaso_id int8,
        primary key (id)
    );

    create table kriteeri_tekstipalanen (
        kriteeri_id int8 not null,
        tekstipalanen_id int8 not null,
        tekstialueet_ORDER int4 not null,
        primary key (kriteeri_id, tekstialueet_ORDER)
    );

    create table osaamistaso (
        id int8 not null,
        otsikko_id int8,
        primary key (id)
    );

    alter table tutkinnonosa add column arviointi_id int8;

    alter table arviointi_kohdealue 
        add constraint UK_arviointi_kohdealue unique (kohdealue_id);

    alter table arviointiasteikko_osaamistaso 
        add constraint UK_arviointiasteikko_osaamistaso unique (osaamistasot_id);

    alter table kohde_kriteeri 
        add constraint UK_kohde_kriteeri unique (kriteerit_id);

    alter table kohdealue_kohde 
        add constraint UK_kohdealue_kohde unique (kohde_id);

    alter table arviointi 
        add constraint FK_arviointi_tekstipalanen
        foreign key (lisatiedot_id) 
        references tekstipalanen;

    alter table arviointi_kohdealue 
        add constraint FK_arviointi_kohdealue_kohdealue
        foreign key (kohdealue_id) 
        references kohdealue;

    alter table arviointi_kohdealue 
        add constraint FK_arviointi_kohdealue_arviointi 
        foreign key (arviointi_id) 
        references arviointi;

    alter table arviointiasteikko_osaamistaso 
        add constraint FK_arviointiasteikko_osaamistaso_osaamistaso
        foreign key (osaamistasot_id) 
        references osaamistaso;

    alter table arviointiasteikko_osaamistaso 
        add constraint FK_arviointiasteikko_osaamistaso_arviointiasteikko 
        foreign key (arviointiasteikko_id) 
        references arviointiasteikko;

    alter table kohde 
        add constraint FK_kohde_arviointiasteikko 
        foreign key (Arviointiasteikko_id) 
        references arviointiasteikko;

    alter table kohde 
        add constraint FK_kohde_tekstipalanen 
        foreign key (otsikko_id) 
        references tekstipalanen;

    alter table kohde_kriteeri 
        add constraint FK_kohde_kriteeri_kriteeri 
        foreign key (kriteerit_id) 
        references kriteeri;

    alter table kohde_kriteeri 
        add constraint FK_kohde_kriteeri_kohde 
        foreign key (kohde_id) 
        references kohde;

    alter table kohdealue 
        add constraint FK_kohdealue_tekstipalanen 
        foreign key (otsikko_id) 
        references tekstipalanen;

    alter table kohdealue_kohde 
        add constraint FK_kohdealue_kohde_kohde 
        foreign key (kohde_id) 
        references kohde;

    alter table kohdealue_kohde 
        add constraint FK_kohdealue_kohde_kohdealue 
        foreign key (kohdealue_id) 
        references kohdealue;

    alter table kriteeri 
        add constraint FK_kriteeri_osaamistaso 
        foreign key (Osaamistaso_id) 
        references osaamistaso;

    alter table kriteeri_tekstipalanen 
        add constraint FK_kriteeri_tekstipalanen_tekstipalanen 
        foreign key (tekstipalanen_id) 
        references tekstipalanen;

    alter table kriteeri_tekstipalanen 
        add constraint FK_kriteeri_tekstipalanen_kriteeri 
        foreign key (kriteeri_id) 
        references kriteeri;

    alter table osaamistaso 
        add constraint FK_osaamistaso_tekstipalanen 
        foreign key (otsikko_id) 
        references tekstipalanen;

    alter table tutkinnonosa 
        add constraint FK_tutkinnonosa_arviointi 
        foreign key (arviointi_id) 
        references arviointi;

