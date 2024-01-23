create table julkaistu_peruste_data_store (
    nimi jsonb,
    kielet jsonb,
    "voimassaoloAlkaa" text,
    "voimassaoloLoppuu" text,
    "siirtymaPaattyy" text,
    paatospvm text,
    id text,
    perusteid text,
    diaarinumero text,
    osaamisalat jsonb,
    tutkintonimikkeet jsonb,
    tutkinnonosat jsonb,
    tila varchar,
    koulutusvienti text,
    koulutustyyppi text,
    tyyppi text,
    oppaankoulutustyypit jsonb,
    laajuus text,
    osaamisalanimet json,
    tutkintonimikkeetnimet json,
    tutkinnonosatnimet json,
    koulutukset jsonb,
    julkaistu timestamp,
    suoritustavat jsonb,
    luotu text,
    koodit jsonb,
    tutkinnonosa jsonb,
    sisaltotyyppi text,
    primary key(perusteid, id)
);

INSERT INTO julkaistu_peruste_data_store SELECT * FROM julkaistu_peruste_data_view;

drop trigger if exists tg_refresh_julkaistu_peruste_data_view on julkaistu_peruste;
drop trigger if exists tg_refresh_julkaistu_peruste_data_view on peruste;
drop function if exists tg_refresh_julkaistu_peruste_data_view;
drop materialized view if exists julkaistu_peruste_data_view;