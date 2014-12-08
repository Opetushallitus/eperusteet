create table tiedote (
        id int8 not null,
        luoja varchar(255),
        luotu timestamp,
        muokattu timestamp,
        muokkaaja varchar(255),
        otsikko_id int8,
        sisalto_id int8,
        primary key (id)
    );

create table tiedote_AUD (
        id int8 not null,
        REV int4 not null,
        REVTYPE int2,
        REVEND int4,
        luoja varchar(255),
        luotu timestamp,
        muokattu timestamp,
        muokkaaja varchar(255),
        otsikko_id int8,
        sisalto_id int8,
        primary key (id, REV)
    );

alter table tiedote
    add constraint FK_k525cnabfro3sy328olrykjha
    foreign key (otsikko_id)
    references tekstipalanen;

alter table tiedote
    add constraint FK_4q8ubsgnh73u4yh330og05bh3
    foreign key (sisalto_id)
    references tekstipalanen;

alter table tiedote_AUD
    add constraint FK_ma2rm5ms8h0xon2jsthm3pord
    foreign key (REV)
    references revinfo;

alter table tiedote_AUD
    add constraint FK_b8317x7w93axl2pp13vwvt2p3
    foreign key (REVEND)
    references revinfo;
