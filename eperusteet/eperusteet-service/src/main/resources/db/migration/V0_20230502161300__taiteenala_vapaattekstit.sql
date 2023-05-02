create table taiteenala_vapaateksti (
    taiteenala_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    primary key (taiteenala_id, kevyttekstikappaleet_order)
);

create table taiteenala_vapaateksti_AUD (
    REV int4 not null,
    taiteenala_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, taiteenala_id, kevyttekstikappale_id, kevyttekstikappaleet_order)
);

alter table taiteenala_vapaateksti
    add constraint FK_gb4g5nfexgpmas71wu19d7jmn
    foreign key (kevyttekstikappale_id)
    references kevyttekstikappale;

alter table taiteenala_vapaateksti
    add constraint FK_jfkd514npgmeryifkyqiho8rj
    foreign key (taiteenala_id)
    references taiteenala;

alter table taiteenala_vapaateksti_AUD
    add constraint FK_qukkv27agug9hho4q3kxn04nx
    foreign key (REV)
    references revinfo;

alter table taiteenala_vapaateksti_AUD
    add constraint FK_tjgbsklddhujjrvxdh86uy5k4
    foreign key (REVEND)
    references revinfo;