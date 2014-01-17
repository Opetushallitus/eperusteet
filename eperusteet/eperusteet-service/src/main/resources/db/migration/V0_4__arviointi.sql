    create table arviointi (
        id int8 not null,
        lisatiedot_id int8,
        primary key (id)
    );

    create table arviointi_arvioinninkohdealue (
        arviointi_id int8 not null,
        arvioinninkohdealue_id int8 not null,
        arvioinninKohdealueet_ORDER int4 not null,
        primary key (arviointi_id, arvioinninKohdealueet_ORDER)
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

    create table arvioinninkohde (
        id int8 not null,
        Arviointiasteikko_id int8,
        otsikko_id int8,
        primary key (id)
    );

    create table arvioinninkohde_osaamistasonkriteeri (
        arvioinninkohde_id int8 not null,
        osaamistasonKriteerit_id int8 not null,
        primary key (arvioinninkohde_id, osaamistasonKriteerit_id)
    );

    create table arvioinninkohdealue (
        id int8 not null,
        otsikko_id int8,
        primary key (id)
    );

    create table arvioinninkohdealue_arvioinninkohde (
        arvioinninkohdealue_id int8 not null,
        arvioinninkohde_id int8 not null,
        arvioinninKohteet_ORDER int4 not null,
        primary key (arvioinninkohdealue_id, arvioinninKohteet_ORDER)
    );

    create table osaamistasonkriteeri (
        id int8 not null,
        Osaamistaso_id int8,
        primary key (id)
    );

    create table osaamistasonkriteeri_tekstipalanen (
        osaamistasonkriteeri_id int8 not null,
        tekstipalanen_id int8 not null,
        kriteerit_ORDER int4 not null,
        primary key (osaamistasonkriteeri_id, kriteerit_ORDER)
    );

    create table osaamistaso (
        id int8 not null,
        otsikko_id int8,
        primary key (id)
    );

    alter table tutkinnonosa add column arviointi_id int8;

    alter table arviointi_arvioinninkohdealue 
        add constraint UK_arviointi_arvioinninkohdealue unique (arvioinninkohdealue_id);

    alter table arviointiasteikko_osaamistaso 
        add constraint UK_arviointiasteikko_osaamistaso unique (osaamistasot_id);

    alter table arvioinninkohde_osaamistasonkriteeri 
        add constraint UK_arvioinninkohde_osaamistasonkriteeri unique (osaamistasonKriteerit_id);

    alter table arvioinninkohdealue_arvioinninkohde 
        add constraint UK_arvioinninkohdealue_arvioinninkohde unique (arvioinninkohde_id);

    alter table arviointi 
        add constraint FK_arviointi_tekstipalanen
        foreign key (lisatiedot_id) 
        references tekstipalanen;

    alter table arviointi_arvioinninkohdealue 
        add constraint FK_arviointi_arvioinninkohdealue_arvioinninkohdealue
        foreign key (arvioinninkohdealue_id) 
        references arvioinninkohdealue;

    alter table arviointi_arvioinninkohdealue 
        add constraint FK_arviointi_arvioinninkohdealue_arviointi 
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

    alter table arvioinninkohde 
        add constraint FK_arvioinninkohde_arviointiasteikko 
        foreign key (Arviointiasteikko_id) 
        references arviointiasteikko;

    alter table arvioinninkohde 
        add constraint FK_arvioinninkohde_tekstipalanen 
        foreign key (otsikko_id) 
        references tekstipalanen;

    alter table arvioinninkohde_osaamistasonkriteeri 
        add constraint FK_arvioinninkohde_osaamistasonkriteeri_osaamistasonkriteeri 
        foreign key (osaamistasonKriteerit_id) 
        references osaamistasonkriteeri;

    alter table arvioinninkohde_osaamistasonkriteeri 
        add constraint FK_arvioinninkohde_osaamistasonkriteeri_arvioinninkohde 
        foreign key (arvioinninkohde_id) 
        references arvioinninkohde;

    alter table arvioinninkohdealue 
        add constraint FK_arvioinninkohdealue_tekstipalanen 
        foreign key (otsikko_id) 
        references tekstipalanen;

    alter table arvioinninkohdealue_arvioinninkohde 
        add constraint FK_arvioinninkohdealue_arvioinninkohde_arvioinninkohde 
        foreign key (arvioinninkohde_id) 
        references arvioinninkohde;

    alter table arvioinninkohdealue_arvioinninkohde 
        add constraint FK_arvioinninkohdealue_arvioinninkohde_arvioinninkohdealue 
        foreign key (arvioinninkohdealue_id) 
        references arvioinninkohdealue;

    alter table osaamistasonkriteeri 
        add constraint FK_osaamistasonkriteeri_osaamistaso 
        foreign key (Osaamistaso_id) 
        references osaamistaso;

    alter table osaamistasonkriteeri_tekstipalanen 
        add constraint FK_osaamistasonkriteeri_tekstipalanen_tekstipalanen 
        foreign key (tekstipalanen_id) 
        references tekstipalanen;

    alter table osaamistasonkriteeri_tekstipalanen 
        add constraint FK_osaamistasonkriteeri_tekstipalanen_osaamistasonkriteeri 
        foreign key (osaamistasonkriteeri_id) 
        references osaamistasonkriteeri;

    alter table osaamistaso 
        add constraint FK_osaamistaso_tekstipalanen 
        foreign key (otsikko_id) 
        references tekstipalanen;

    alter table tutkinnonosa 
        add constraint FK_tutkinnonosa_arviointi 
        foreign key (arviointi_id) 
        references arviointi;

