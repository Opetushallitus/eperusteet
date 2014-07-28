    alter table suoritustapa
        add tutkinnon_rakenne_id int8;

    create table tutkinnonosaviite (
        id int8 not null,
        jarjestys int4,
        laajuus int4,
        yksikko int4,
        suoritustapa_id int8,
        tutkinnonosa_id int8,
        primary key (id)
    );

    create table tutkinnonosaviite_AUD (
        id int8 not null,
        REV int4 not null,
        REVTYPE int2,
        REVEND int4,
        jarjestys int4,
        laajuus int4,
        yksikko int4,
        suoritustapa_id int8,
        tutkinnonosa_id int8,
        primary key (id, REV)
    );


    create table tutkinnon_rakenne (
        tyyppi varchar(31) not null,
        id int8 not null,
        koko_max int4,
        koko_min int4,
        laajuus_max int4,
        laajuus_min int4,
        laajuus_yksikko varchar(255),
        pakollinen boolean,
        moduuli_id int8,
        kuvaus_id int8,
        nimi_id int8,
        rakenneosa_tutkinnonosaviite int8,
        osat_ORDER int4,
        primary key (id)
    );

    create table tutkinnon_rakenne_AUD (
        tyyppi varchar(31) not null,
        id int8 not null,
        REV int4 not null,
        REVTYPE int2,
        REVEND int4,
        moduuli_id int8,
        pakollinen boolean,
        rakenneosa_tutkinnonosaviite int8,
        koko_max int4,
        koko_min int4,
        laajuus_max int4,
        laajuus_min int4,
        laajuus_yksikko varchar(255),
        kuvaus_id int8,
        nimi_id int8,
        primary key (id, REV)
    );

    alter table tutkinnon_rakenne
        add constraint FK_tutkinnon_rakenne_tutkinnon_rakenne_moduuli
        foreign key (moduuli_id)
        references tutkinnon_rakenne;

    alter table tutkinnon_rakenne
        add constraint FK_tutkinnon_rakenne_kuvaus_tekstipalanen
        foreign key (kuvaus_id)
        references tekstipalanen;

    alter table tutkinnon_rakenne
        add constraint FK_tutkinnon_rakenne_nimi_tekstipalanen
        foreign key (nimi_id)
        references tekstipalanen;

    alter table tutkinnon_rakenne
        add constraint FK_tutkinnon_rakenne_tutkinnonosaviite
        foreign key (rakenneosa_tutkinnonosaviite)
        references tutkinnonosaviite;

    alter table tutkinnon_rakenne_AUD
        add constraint FK_tutkinnon_rakenne_aud_revinfo_rev
        foreign key (REV)
        references REVINFO;

    alter table tutkinnon_rakenne_AUD
        add constraint FK_tutkinnon_rakenne_aud_revinfo_revend
        foreign key (REVEND)
        references REVINFO;
