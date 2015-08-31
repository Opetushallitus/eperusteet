create table valmatelma_osaamisenarviointi (
    id bigint unique not null,
    kohde_id bigint,
    oatekstina_id bigint
);

create table valmatelma_osaamisenarviointi_aud (
    id bigint not null,
    kohde_id bigint,
    oatekstina_id bigint,
    rev integer not null,
    revtype smallint,
    revend integer
);

create table osaalue_valmatelma (
    id bigint unique not null,
    osaamistavoite_id bigint references valmatelma_osaamisenarviointi(id),
    osaamisenarviointi_id bigint references valmatelma_osaamisenarviointi(id)
);

create table osaalue_valmatelma_aud (
    id bigint not null,
    osaamistavoite_id bigint,
    osaamisenarviointi_id bigint,
    rev integer not null,
    revtype smallint,
    revend integer
);

alter table tutkinnonosa_osaalue add column valmatelmasisalto_id bigint;

alter table tutkinnonosa_osaalue_aud add column valmatelmasisalto_id bigint;
