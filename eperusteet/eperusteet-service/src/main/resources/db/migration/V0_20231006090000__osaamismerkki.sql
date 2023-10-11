create table osaamismerkki (
                               id int8 not null,
                               luoja varchar(255),
                               luotu timestamp,
                               muokattu timestamp,
                               muokkaaja varchar(255),
                               tila varchar(255) not null,
                               voimassaolo_alkaa timestamp not null,
                               voimassaolo_loppuu timestamp,
                               kategoria_id int8 not null,
                               nimi_id int8 not null,
                               primary key (id)
);

create table osaamismerkki_AUD (
                                   id int8 not null,
                                   REV int4 not null,
                                   REVTYPE int2,
                                   REVEND int4,
                                   luoja varchar(255),
                                   luotu timestamp,
                                   muokattu timestamp,
                                   muokkaaja varchar(255),
                                   tila varchar(255),
                                   voimassaolo_alkaa timestamp,
                                   voimassaolo_loppuu timestamp,
                                   kategoria_id int8,
                                   nimi_id int8,
                                   primary key (id, REV)
);

create table osaamismerkki_arviointikriteeri (
                                                 id int8 not null,
                                                 arviointikriteeri_id int8 not null,
                                                 primary key (id)
);

create table osaamismerkki_arviointikriteerit (
                                                  osaamismerkki_id int8 not null,
                                                  osaamismerkki_arviointikriteeri_id int8 not null,
                                                  arviointikriteerit_ORDER int4 not null,
                                                  primary key (osaamismerkki_id, arviointikriteerit_ORDER)
);

create table osaamismerkki_arviointikriteerit_AUD (
                                                      REV int4 not null,
                                                      osaamismerkki_id int8 not null,
                                                      osaamismerkki_arviointikriteeri_id int8 not null,
                                                      arviointikriteerit_ORDER int4 not null,
                                                      REVTYPE int2,
                                                      REVEND int4,
                                                      primary key (REV, osaamismerkki_id, osaamismerkki_arviointikriteeri_id, arviointikriteerit_ORDER)
);

create table osaamismerkki_kategoria (
                                         id int8 not null,
                                         luoja varchar(255),
                                         luotu timestamp,
                                         muokattu timestamp,
                                         muokkaaja varchar(255),
                                         liite_id uuid not null,
                                         nimi_id int8 not null,
                                         primary key (id)
);

create table osaamismerkki_kategoria_AUD (
                                             id int8 not null,
                                             REV int4 not null,
                                             REVTYPE int2,
                                             REVEND int4,
                                             luoja varchar(255),
                                             luotu timestamp,
                                             muokattu timestamp,
                                             muokkaaja varchar(255),
                                             liite_id uuid,
                                             nimi_id int8,
                                             primary key (id, REV)
);

create table osaamismerkki_osaamistavoite (
                                              id int8 not null,
                                              osaamistavoite_id int8 not null,
                                              primary key (id)
);

create table osaamismerkki_osaamistavoite_AUD (
                                                  id int8 not null,
                                                  REV int4 not null,
                                                  REVTYPE int2,
                                                  REVEND int4,
                                                  osaamistavoite_id int8,
                                                  primary key (id, REV)
);

create table osaamismerkki_osaamistavoitteet (
                                                 osaamismerkki_id int8 not null,
                                                 osaamismerkki_osaamistavoite_id int8 not null,
                                                 osaamistavoitteet_ORDER int4 not null,
                                                 primary key (osaamismerkki_id, osaamistavoitteet_ORDER)
);

create table osaamismerkki_osaamistavoitteet_AUD (
                                                     REV int4 not null,
                                                     osaamismerkki_id int8 not null,
                                                     osaamismerkki_osaamistavoite_id int8 not null,
                                                     osaamistavoitteet_ORDER int4 not null,
                                                     REVTYPE int2,
                                                     REVEND int4,
                                                     primary key (REV, osaamismerkki_id, osaamismerkki_osaamistavoite_id, osaamistavoitteet_ORDER)
);

alter table osaamismerkki_arviointikriteerit
    add constraint UK_crj8cm5jgfbrg5c595x6l94wn  unique (osaamismerkki_arviointikriteeri_id);

alter table osaamismerkki_osaamistavoitteet
    add constraint UK_7lhng63ms5r15dn8u575eg0y0  unique (osaamismerkki_osaamistavoite_id);

alter table osaamismerkki
    add constraint FK_mp8o1vv1mk9473662ror253p3
        foreign key (kategoria_id)
            references osaamismerkki_kategoria;

alter table osaamismerkki
    add constraint FK_bam9blna3qtwo3y4edbfj68wk
        foreign key (nimi_id)
            references tekstipalanen;

alter table osaamismerkki_AUD
    add constraint FK_hfjw6windq2mhxjp71qcg1op
        foreign key (REV)
            references revinfo;

alter table osaamismerkki_AUD
    add constraint FK_7r7d8rcm4dtrx40v59fucl448
        foreign key (REVEND)
            references revinfo;

alter table osaamismerkki_arviointikriteeri
    add constraint FK_nf0v10d1poi2oaxibg68ox7qx
        foreign key (arviointikriteeri_id)
            references tekstipalanen;

alter table osaamismerkki_arviointikriteerit
    add constraint FK_crj8cm5jgfbrg5c595x6l94wn
        foreign key (osaamismerkki_arviointikriteeri_id)
            references osaamismerkki_arviointikriteeri;

alter table osaamismerkki_arviointikriteerit
    add constraint FK_8es0h9l5eqp9kvpi11qqnjw8n
        foreign key (osaamismerkki_id)
            references osaamismerkki;

alter table osaamismerkki_arviointikriteerit_AUD
    add constraint FK_4u5v5erst8a5nlotob7drwhg3
        foreign key (REV)
            references revinfo;

alter table osaamismerkki_arviointikriteerit_AUD
    add constraint FK_h2219j6w6ktawmy6soi4rrl0k
        foreign key (REVEND)
            references revinfo;

alter table osaamismerkki_kategoria
    add constraint FK_ob0vhrm11pgbsrfvhin43r1xs
        foreign key (liite_id)
            references liite;

alter table osaamismerkki_kategoria
    add constraint FK_hs9k3stn776c8h5bn3lpp8rm2
        foreign key (nimi_id)
            references tekstipalanen;

alter table osaamismerkki_kategoria_AUD
    add constraint FK_13ka51qyq3j6e6amjso1k5a61
        foreign key (REV)
            references revinfo;

alter table osaamismerkki_kategoria_AUD
    add constraint FK_pfdwbf304h21psvp9eqrnbfu
        foreign key (REVEND)
            references revinfo;

alter table osaamismerkki_osaamistavoite
    add constraint FK_cua2uhvki4faj0af1yr7lnag7
        foreign key (osaamistavoite_id)
            references tekstipalanen;

alter table osaamismerkki_osaamistavoite_AUD
    add constraint FK_o5me1rlsxvcgtpulagv8b59f8
        foreign key (REV)
            references revinfo;

alter table osaamismerkki_osaamistavoite_AUD
    add constraint FK_aw0ld44la65782axkjo1p6exy
        foreign key (REVEND)
            references revinfo;

alter table osaamismerkki_osaamistavoitteet
    add constraint FK_7lhng63ms5r15dn8u575eg0y0
        foreign key (osaamismerkki_osaamistavoite_id)
            references osaamismerkki_osaamistavoite;

alter table osaamismerkki_osaamistavoitteet
    add constraint FK_87vtmgtk2j19unvlpmbijjsr9
        foreign key (osaamismerkki_id)
            references osaamismerkki;

alter table osaamismerkki_osaamistavoitteet_AUD
    add constraint FK_omqb33jbxb8ixqmqpvbnohpyj
        foreign key (REV)
            references revinfo;

alter table osaamismerkki_osaamistavoitteet_AUD
    add constraint FK_aewq6h2ir0sk2au45yppr0hll
        foreign key (REVEND)
            references revinfo;
