create table koto_kielitaitotaso (
    id int8 not null,
    kuvaus_id int8,
    nimiKoodi_id int8,
    primary key (id)
);

create table koto_kielitaitotaso_AUD (
    id int8 not null,
    REV int4 not null,
    kuvaus_id int8,
    nimiKoodi_id int8,
    primary key (id, REV)
);

create table koto_kielitaitotaso_taitotasot (
    koto_kielitaitotaso_id int8 not null,
    taitotaso_id int8 not null,
    taitotasot_ORDER int4 not null,
    primary key (koto_kielitaitotaso_id, taitotasot_ORDER)
);

create table koto_kielitaitotaso_taitotasot_AUD (
    REV int4 not null,
    koto_kielitaitotaso_id int8 not null,
    taitotaso_id int8 not null,
    taitotasot_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, koto_kielitaitotaso_id, taitotaso_id, taitotasot_ORDER)
);

create table koto_opinto (
    id int8 not null,
    kuvaus_id int8,
    nimiKoodi_id int8,
    primary key (id)
);

create table koto_opinto_AUD (
    id int8 not null,
    REV int4 not null,
    kuvaus_id int8,
    nimiKoodi_id int8,
    primary key (id, REV)
);

create table koto_opinto_taitotasot (
    koto_opinto_id int8 not null,
    taitotaso_id int8 not null,
    taitotasot_ORDER int4 not null,
    primary key (koto_opinto_id, taitotasot_ORDER)
);

create table koto_opinto_taitotasot_AUD (
    REV int4 not null,
    koto_opinto_id int8 not null,
    taitotaso_id int8 not null,
    taitotasot_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, koto_opinto_id, taitotaso_id, taitotasot_ORDER)
);

create table koto_taitotaso (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    aihealueet_id int8,
    kielenkayttotarkoitus_id int8,
    nimi_id int8,
    opiskelijantaidot_id int8,
    tavoitteet_id int8,
    viestintataidot_id int8,
    primary key (id)
);

create table koto_taitotaso_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    aihealueet_id int8,
    kielenkayttotarkoitus_id int8,
    nimi_id int8,
    opiskelijantaidot_id int8,
    tavoitteet_id int8,
    viestintataidot_id int8,
    primary key (id, REV)
);

alter table koto_kielitaitotaso_taitotasot
    add constraint UK_a9qe7qeac4tv3jkt3vyjjtils  unique (taitotaso_id);

alter table koto_opinto_taitotasot
    add constraint UK_c0c5wefeu8efqcjtn1xs8lb5t  unique (taitotaso_id);


alter table koto_kielitaitotaso
    add constraint FK_k27dhif5kgywm32va41fk3xbb
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table koto_kielitaitotaso
    add constraint FK_mftlygyjk8lvjc371wk8i7npn
    foreign key (nimiKoodi_id)
    references koodi;

alter table koto_kielitaitotaso
    add constraint FK_ebuoifio5xonsrfh4ekb3dlsy
    foreign key (id)
    references perusteenosa;

alter table koto_kielitaitotaso_AUD
    add constraint FK_lwwud5gj2cc3cgdd01kg0b98s
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table koto_kielitaitotaso_taitotasot
    add constraint FK_a9qe7qeac4tv3jkt3vyjjtils
    foreign key (taitotaso_id)
    references koto_taitotaso;

alter table koto_kielitaitotaso_taitotasot
    add constraint FK_q598k0d0944yubib7jg294ms2
    foreign key (koto_kielitaitotaso_id)
    references koto_kielitaitotaso;

alter table koto_kielitaitotaso_taitotasot_AUD
    add constraint FK_dtwf2kjr6yv48wbo89suywjip
    foreign key (REV)
    references revinfo;

alter table koto_kielitaitotaso_taitotasot_AUD
    add constraint FK_egt7m1a3kymwmu94sg7yywus6
    foreign key (REVEND)
    references revinfo;

alter table koto_opinto
    add constraint FK_a650jnpgkpfigqoq0h4w7sd2n
    foreign key (kuvaus_id)
    references tekstipalanen;

alter table koto_opinto
    add constraint FK_1jfij69dndgt7vhpdu66ll4bl
    foreign key (nimiKoodi_id)
    references koodi;

alter table koto_opinto
    add constraint FK_r2dgwweu86bvv28su22pjfpm7
    foreign key (id)
    references perusteenosa;

alter table koto_opinto_AUD
    add constraint FK_8dlk2hg87pl720o9elk48v57y
    foreign key (id, REV)
    references perusteenosa_AUD;

alter table koto_opinto_taitotasot
    add constraint FK_c0c5wefeu8efqcjtn1xs8lb5t
    foreign key (taitotaso_id)
    references koto_taitotaso;

alter table koto_opinto_taitotasot
    add constraint FK_po9n0yxyegje6x5n05yhnynfy
    foreign key (koto_opinto_id)
    references koto_opinto;

alter table koto_opinto_taitotasot_AUD
    add constraint FK_ds5x6fghxmryve5lj0co73qp5
    foreign key (REV)
    references revinfo;

alter table koto_opinto_taitotasot_AUD
    add constraint FK_f10weg1vpqhin2464pnci1dqr
    foreign key (REVEND)
    references revinfo;

alter table koto_taitotaso
    add constraint FK_fsn3as6um1oxlqgycgh8eld2t
    foreign key (aihealueet_id)
    references tekstipalanen;

alter table koto_taitotaso
    add constraint FK_49lp1fiwgwbtesvc0do2q5r8y
    foreign key (kielenkayttotarkoitus_id)
    references tekstipalanen;

alter table koto_taitotaso
    add constraint FK_b0lf4hv6tmjaro2omcrfoxwr7
    foreign key (nimi_id)
    references koodi;

alter table koto_taitotaso
    add constraint FK_7mair0kdggf2setqhj6qpbq05
    foreign key (opiskelijantaidot_id)
    references tekstipalanen;

alter table koto_taitotaso
    add constraint FK_4q3cxwb4c6cmml045h86dmgi7
    foreign key (tavoitteet_id)
    references tekstipalanen;

alter table koto_taitotaso
    add constraint FK_i76dubo7grngoja15gxc1i6dp
    foreign key (viestintataidot_id)
    references tekstipalanen;

alter table koto_taitotaso_AUD
    add constraint FK_blweue3jxr3ryd1g4jp1qjldq
    foreign key (REV)
    references revinfo;

alter table koto_taitotaso_AUD
    add constraint FK_a52vjvwbe5p5v2lf6pb8jm5s0
    foreign key (REVEND)
    references revinfo;
