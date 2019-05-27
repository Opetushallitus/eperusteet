create table geneerinen_osaamistason_kriteeri (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    osaamistaso_id int8 not null,
    primary key (id)
);

create table geneerinen_osaamistason_kriteeri_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    osaamistaso_id int8,
    primary key (id, REV)
);

create table geneerinen_osaamistason_kriteeri_tekstipalanen (
    geneerinen_osaamistason_kriteeri_id int8 not null,
    kriteerit_id int8 not null,
    kriteerit_ORDER int4 not null,
    primary key (geneerinen_osaamistason_kriteeri_id, kriteerit_ORDER)
);

create table geneerinen_osaamistason_kriteeri_tekstipalanen_AUD (
    REV int4 not null,
    geneerinen_osaamistason_kriteeri_id int8 not null,
    kriteerit_id int8 not null,
    kriteerit_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, geneerinen_osaamistason_kriteeri_id, kriteerit_id, kriteerit_ORDER)
);

create table geneerinenarviointiasteikko (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    julkaistu boolean not null,
    arviointiAsteikko_id int8 not null,
    kohde_id int8,
    nimi_id int8,
    primary key (id)
);

create table geneerinenarviointiasteikko_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    julkaistu boolean,
    arviointiAsteikko_id int8,
    kohde_id int8,
    nimi_id int8,
    primary key (id, REV)
);

create table geneerinenarviointiasteikko_geneerisenosaamistasonkriteeri (
    geneerinenarviointiasteikko_id int8 not null,
    osaamistasonKriteerit_id int8 not null,
    primary key (geneerinenarviointiasteikko_id, osaamistasonKriteerit_id)
);

create table geneerinenarviointiasteikko_geneerisenosaamistasonkriteeri_AUD (
    REV int4 not null,
    geneerinenarviointiasteikko_id int8 not null,
    osaamistasonKriteerit_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, geneerinenarviointiasteikko_id, osaamistasonKriteerit_id)
);

create table geneerisenosaamistasonkriteeri (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    osaamistaso_id int8 not null,
    primary key (id)
);

create table geneerisenosaamistasonkriteeri_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    osaamistaso_id int8,
    primary key (id, REV)
);

create table geneerisenosaamistasonkriteeri_tekstipalanen (
    geneerisenosaamistasonkriteeri_id int8 not null,
    kriteerit_id int8 not null,
    kriteerit_ORDER int4 not null,
    primary key (geneerisenosaamistasonkriteeri_id, kriteerit_ORDER)
);

create table geneerisenosaamistasonkriteeri_tekstipalanen_AUD (
    REV int4 not null,
    geneerisenosaamistasonkriteeri_id int8 not null,
    kriteerit_id int8 not null,
    kriteerit_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, geneerisenosaamistasonkriteeri_id, kriteerit_id, kriteerit_ORDER)
);

alter table geneerinen_osaamistason_kriteeri 
    add constraint FK_9x4aadeyq5se4cl89wx4em1n 
    foreign key (osaamistaso_id) 
    references osaamistaso;

alter table geneerinen_osaamistason_kriteeri_AUD 
    add constraint FK_5q4ys6cvous4mhs5pct8iyfx2 
    foreign key (REV) 
    references revinfo;

alter table geneerinen_osaamistason_kriteeri_AUD 
    add constraint FK_biqhvadqfr72el4dbn184lur9 
    foreign key (REVEND) 
    references revinfo;

alter table geneerinen_osaamistason_kriteeri_tekstipalanen 
    add constraint FK_c5h9ttn05jlie6at80lqjedig 
    foreign key (kriteerit_id) 
    references tekstipalanen;

alter table geneerinen_osaamistason_kriteeri_tekstipalanen 
    add constraint FK_c6gpjq6t2pgpvg3ue75fw92f4 
    foreign key (geneerinen_osaamistason_kriteeri_id) 
    references geneerinen_osaamistason_kriteeri;

alter table geneerinen_osaamistason_kriteeri_tekstipalanen_AUD 
    add constraint FK_abk8vkcili5v3nqybambnaget 
    foreign key (REV) 
    references revinfo;

alter table geneerinen_osaamistason_kriteeri_tekstipalanen_AUD 
    add constraint FK_qhmor8i4aiqmesfkmguej2aej 
    foreign key (REVEND) 
    references revinfo;

alter table geneerinenarviointiasteikko 
    add constraint FK_bnt2lalnbmxrj35k5f9i3902y 
    foreign key (arviointiAsteikko_id) 
    references arviointiasteikko;

alter table geneerinenarviointiasteikko 
    add constraint FK_lpb3qr0jsyh4ycfn3ru295hym 
    foreign key (kohde_id) 
    references tekstipalanen;

alter table geneerinenarviointiasteikko 
    add constraint FK_ohhwhq5set9ngyv5jsdmbnmhd 
    foreign key (nimi_id) 
    references tekstipalanen;

alter table geneerinenarviointiasteikko_AUD 
    add constraint FK_d62aehoi9m9ad0dykly3ndtfr 
    foreign key (REV) 
    references revinfo;

alter table geneerinenarviointiasteikko_AUD 
    add constraint FK_d629ok42e5pw0m3e2ggy0ovix 
    foreign key (REVEND) 
    references revinfo;

alter table geneerinenarviointiasteikko_geneerisenosaamistasonkriteeri 
    add constraint FK_3ayo9s5x6a2bwx0lrlxu08upf 
    foreign key (osaamistasonKriteerit_id) 
    references geneerisenosaamistasonkriteeri;

alter table geneerinenarviointiasteikko_geneerisenosaamistasonkriteeri 
    add constraint FK_6t3lvhnu4cnlejyo5ak5i8k7p 
    foreign key (geneerinenarviointiasteikko_id) 
    references geneerinenarviointiasteikko;

alter table geneerinenarviointiasteikko_geneerisenosaamistasonkriteeri_AUD 
    add constraint FK_lh1kdi69ls9e3holjgagm1arh 
    foreign key (REV) 
    references revinfo;

alter table geneerinenarviointiasteikko_geneerisenosaamistasonkriteeri_AUD 
    add constraint FK_12t3epe5e3sxh3sgc30qw9tfi 
    foreign key (REVEND) 
    references revinfo;

alter table geneerisenosaamistasonkriteeri 
    add constraint FK_rmfj7i2dyhnvyotmv52bculeg 
    foreign key (osaamistaso_id) 
    references osaamistaso;

alter table geneerisenosaamistasonkriteeri_AUD 
    add constraint FK_g2v99fhjkm80i18vlhvgjp7ox 
    foreign key (REV) 
    references revinfo;

alter table geneerisenosaamistasonkriteeri_AUD 
    add constraint FK_tfv3eoy4bg6ld9tjaqotnrc14 
    foreign key (REVEND) 
    references revinfo;

alter table geneerisenosaamistasonkriteeri_tekstipalanen 
    add constraint FK_q79fjhlhere4puqxrkffx1pms 
    foreign key (kriteerit_id) 
    references tekstipalanen;

alter table geneerisenosaamistasonkriteeri_tekstipalanen 
    add constraint FK_gll94codhb68rwipuynjt55aj 
    foreign key (geneerisenosaamistasonkriteeri_id) 
    references geneerisenosaamistasonkriteeri;

alter table geneerisenosaamistasonkriteeri_tekstipalanen_AUD 
    add constraint FK_ggen8abwrkx109ucobn1x4tno 
    foreign key (REV) 
    references revinfo;

alter table geneerisenosaamistasonkriteeri_tekstipalanen_AUD 
    add constraint FK_kj33t3d0viq1byqcinup2dvqc 
    foreign key (REVEND) 
    references revinfo;
