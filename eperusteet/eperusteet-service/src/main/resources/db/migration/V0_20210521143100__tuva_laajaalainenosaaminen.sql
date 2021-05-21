create table tuva_laajaalainenosaaminen (
    liite boolean not null,
    id int8 not null,
    nimiKoodi_id int8,
    teksti_id int8,
    primary key (id)
);

create table tuva_laajaalainenosaaminen_AUD (
    id int8 not null,
    REV int4 not null,
    liite boolean,
    nimiKoodi_id int8,
    teksti_id int8,
    primary key (id, REV)
);

alter table tuva_laajaalainenosaaminen
    add constraint FK_2191n85q2tt5cfvhjft3h6riq
    foreign key (nimiKoodi_id)
    references koodi;

alter table tuva_laajaalainenosaaminen
    add constraint FK_bjtpdcfgmdgy5ylrle7hc3puv
    foreign key (teksti_id)
    references tekstipalanen;

alter table tuva_laajaalainenosaaminen
    add constraint FK_22wykmk3wtvdfhssl3ifxg1o4
    foreign key (id)
    references perusteenosa;

alter table tuva_laajaalainenosaaminen_AUD
    add constraint FK_cp69uj5o0ak2h0m41x6930vrw
    foreign key (id, REV)
    references perusteenosa_AUD;
