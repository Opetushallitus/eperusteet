create table yl_vlkokonaisuus_vapaateksti (
    vuosiluokkakokonaisuudet_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    primary key (vuosiluokkakokonaisuudet_id, kevyttekstikappaleet_order)
);

create table yl_vlkokonaisuus_vapaateksti_AUD (
    REV int4 not null,
    vuosiluokkakokonaisuudet_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, vuosiluokkakokonaisuudet_id, kevyttekstikappale_id, kevyttekstikappaleet_order)
);

alter table yl_vlkokonaisuus_vapaateksti
    add constraint FK_klwbk833lfp2fxaxl2875x2xn
    foreign key (kevyttekstikappale_id)
    references kevyttekstikappale;

alter table yl_vlkokonaisuus_vapaateksti
    add constraint FK_9vhtbvxkf5j9otve690jo5mq
    foreign key (vuosiluokkakokonaisuudet_id)
    references yl_vlkokonaisuus;

alter table yl_vlkokonaisuus_vapaateksti_AUD
    add constraint FK_46wex3mx5o2662gky6hq95kwq
    foreign key (REV)
    references revinfo;

alter table yl_vlkokonaisuus_vapaateksti_AUD
    add constraint FK_1efsj7adb2cqabcty6ctm0d95
    foreign key (REVEND)
    references revinfo;


create table yl_oppiaine_vapaateksti (
    oppiaine_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    primary key (oppiaine_id, kevyttekstikappaleet_order)
);

create table yl_oppiaine_vapaateksti_AUD (
    REV int4 not null,
    oppiaine_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, kevyttekstikappale_id, kevyttekstikappaleet_order)
);

alter table yl_oppiaine_vapaateksti
    add constraint FK_4uko3x5kl67o9erqpoarwyw8e
    foreign key (kevyttekstikappale_id)
    references kevyttekstikappale;

alter table yl_oppiaine_vapaateksti
    add constraint FK_h223dotq55aebpwd4gx84k2e6
    foreign key (oppiaine_id)
    references yl_oppiaine;

alter table yl_oppiaine_vapaateksti_AUD
    add constraint FK_5glvt65gbthn1a5cpsqjxeoxr
    foreign key (REV)
    references revinfo;

alter table yl_oppiaine_vapaateksti_AUD
    add constraint FK_fu7pmf7yva8jqilmfkkqrf20n
    foreign key (REVEND)
    references revinfo;

create table yl_oppiaineen_vlkok_vapaateksti (
    oppiaine_vlk_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    primary key (oppiaine_vlk_id, kevyttekstikappaleet_order)
);

create table yl_oppiaineen_vlkok_vapaateksti_AUD (
    REV int4 not null,
    oppiaine_vlk_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_vlk_id, kevyttekstikappale_id, kevyttekstikappaleet_order)
);

alter table yl_oppiaineen_vlkok_vapaateksti
    add constraint FK_ppjphot0cg44a5m6j7csxp0re
    foreign key (kevyttekstikappale_id)
    references kevyttekstikappale;

alter table yl_oppiaineen_vlkok_vapaateksti
    add constraint FK_fljfttmt3samj86j6p1p9r1nn
    foreign key (oppiaine_vlk_id)
    references yl_oppiaineen_vlkok;

alter table yl_oppiaineen_vlkok_vapaateksti_AUD
    add constraint FK_bp3uv8jl1uc0bmqqdos8qmi7x
    foreign key (REV)
    references revinfo;

alter table yl_oppiaineen_vlkok_vapaateksti_AUD
    add constraint FK_stvknk9lqmy1ymvswicxwcf2k
    foreign key (REVEND)
    references revinfo;