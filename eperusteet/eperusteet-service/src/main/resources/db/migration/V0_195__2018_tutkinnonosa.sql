CREATE SCHEMA tutkinnonosa_2018;

create table tutkinnonosa_2018.ammattitaitovaatimus (
    id int8 not null,
    tunniste uuid,
    arviointiAsteikko_id int8 not null,
    nimi_id int8,
    primary key (id)
);

create table tutkinnonosa_2018.ammattitaitovaatimus_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    tunniste uuid,
    arviointiAsteikko_id int8,
    nimi_id int8,
    primary key (id, REV)
);

create table tutkinnonosa_2018.osaamistason_kriteerit (
    id int8 not null,
    tunniste uuid,
    osaamistaso_id int8,
    primary key (id)
);

create table tutkinnonosa_2018.osaamistason_kriteerit_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    tunniste uuid,
    osaamistaso_id int8,
    primary key (id, REV)
);

create table tutkinnonosa_2018.tutkinnonosa_ammattitaitovaatimus (
    tutkinnonosa_id int8 not null references tutkinnonosa(id),
    ammattitaitovaatimukset2018_id int8 not null references tutkinnonosa_2018.ammattitaitovaatimus(id),
    jarjestys int4 not null,
    primary key (tutkinnonosa_id, jarjestys)
);

create table tutkinnonosa_2018.tutkinnonosa_ammattitaitovaatimus_AUD (
    REV int4 not null,
    tutkinnonosa_id int8,
    ammattitaitovaatimukset2018_id int8,
    jarjestys int4,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tutkinnonosa_id, ammattitaitovaatimukset2018_id, jarjestys)
);

