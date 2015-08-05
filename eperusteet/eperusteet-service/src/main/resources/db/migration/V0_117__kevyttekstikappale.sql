CREATE TABLE kevyttekstikappale(
    id bigint NOT NULL PRIMARY KEY,
    nimi_id bigint REFERENCES tekstipalanen(id),
    teksti_id bigint REFERENCES tekstipalanen(id),
    luoja varchar(255),
    muokkaaja varchar(255),
    muokattu timestamp without time zone,
    luotu timestamp without time zone
);

CREATE TABLE kevyttekstikappale_aud(
    id bigint NOT NULL PRIMARY KEY,
    nimi_id bigint REFERENCES tekstipalanen(id),
    teksti_id bigint REFERENCES tekstipalanen(id),
    luoja varchar(255),
    muokkaaja varchar(255),
    muokattu timestamp without time zone,
    luotu timestamp without time zone,
    rev integer,
    revtype smallint,
    revend integer
);

CREATE TABLE tutkinnonosa_tutkinnonosa_kevyttekstikappale (
    tutkinnonosa_id bigint NOT NULL REFERENCES tutkinnonosa(id),
    kevyttekstikappale_id bigint NOT NULL REFERENCES kevyttekstikappale(id),
    kevyttekstikappaleet_order INTEGER
);

CREATE TABLE tutkinnonosa_tutkinnonosa_kevyttekstikappale_aud (
    tutkinnonosa_id bigint NOT NULL REFERENCES tutkinnonosa(id),
    kevyttekstikappale_id bigint NOT NULL REFERENCES kevyttekstikappale(id),
    kevyttekstikappaleet_order INTEGER,
    rev integer,
    revtype smallint,
    revend integer
);