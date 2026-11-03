create table taiteenalavaihe (
    id int8 not null,
    koodi_id int8,
    laajuus int4,
    kuvaus_id int8,
    primary key (id)
);

create table taiteenalavaihe_AUD (
    id int8 not null,
    REV int4 not null,
    koodi_id int8,
    laajuus int4,
    kuvaus_id int8,
    primary key (id, REV)
);

alter table taiteenalavaihe
    add constraint FK_taiteenalavaihe_perusteenosa
    foreign key (id)
    references perusteenosa;

alter table taiteenalavaihe
    add constraint FK_taiteenalavaihe_koodi
    foreign key (koodi_id)
    references koodi;

alter table taiteenalavaihe
    add constraint FK_taiteenalavaihe_kuvaus
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table taiteenalavaihe_AUD
    add constraint FK_taiteenalavaihe_AUD_perusteenosa_AUD
    foreign key (id, REV)
    references perusteenosa_AUD;

create table taiteenalavaihe_tavoitealueet (
    taiteenalavaihe_id int8 not null,
    tavoitealue_id int8 not null,
    tavoitealueet_ORDER int4 not null,
    primary key (taiteenalavaihe_id, tavoitealueet_ORDER)
);

create table taiteenalavaihe_tavoitealueet_AUD (
    REV int4 not null,
    taiteenalavaihe_id int8 not null,
    tavoitealue_id int8 not null,
    tavoitealueet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, taiteenalavaihe_id, tavoitealue_id, tavoitealueet_ORDER)
);

alter table taiteenalavaihe_tavoitealueet
    add constraint UK_taiteenalavaihe_tavoitealueet_tavoitealue unique (tavoitealue_id);

alter table taiteenalavaihe_tavoitealueet
    add constraint FK_taiteenalavaihe_tavoitealueet_tavoitealue
    foreign key (tavoitealue_id)
    references tavoitealue;

alter table taiteenalavaihe_tavoitealueet
    add constraint FK_taiteenalavaihe_tavoitealueet_taiteenalavaihe
    foreign key (taiteenalavaihe_id)
    references taiteenalavaihe;

alter table taiteenalavaihe_tavoitealueet_AUD
    add constraint FK_taiteenalavaihe_tavoitealueet_AUD_REV
    foreign key (REV)
    references revinfo;

alter table taiteenalavaihe_tavoitealueet_AUD
    add constraint FK_taiteenalavaihe_tavoitealueet_AUD_REVEND
    foreign key (REVEND)
    references revinfo;

create table taiteenalavaihe_vapaateksti (
    taiteenalavaihe_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    primary key (taiteenalavaihe_id, kevyttekstikappaleet_order)
);

create table taiteenalavaihe_vapaateksti_AUD (
    REV int4 not null,
    taiteenalavaihe_id int8 not null,
    kevyttekstikappale_id int8 not null,
    kevyttekstikappaleet_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, taiteenalavaihe_id, kevyttekstikappale_id, kevyttekstikappaleet_order)
);

alter table taiteenalavaihe_vapaateksti
    add constraint FK_taiteenalavaihe_vapaateksti_kevyttekstikappale
    foreign key (kevyttekstikappale_id)
    references kevyttekstikappale;

alter table taiteenalavaihe_vapaateksti
    add constraint FK_taiteenalavaihe_vapaateksti_taiteenalavaihe
    foreign key (taiteenalavaihe_id)
    references taiteenalavaihe;

alter table taiteenalavaihe_vapaateksti_AUD
    add constraint FK_taiteenalavaihe_vapaateksti_AUD_REV
    foreign key (REV)
    references revinfo;

alter table taiteenalavaihe_vapaateksti_AUD
    add constraint FK_taiteenalavaihe_vapaateksti_AUD_REVEND
    foreign key (REVEND)
    references revinfo;
