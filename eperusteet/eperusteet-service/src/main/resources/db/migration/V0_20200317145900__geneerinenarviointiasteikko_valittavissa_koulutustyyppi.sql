ALTER TABLE geneerinenarviointiasteikko ADD COLUMN valittavissa BOOLEAN DEFAULT TRUE;

ALTER TABLE geneerinenarviointiasteikko_aud ADD COLUMN valittavissa BOOLEAN;

create table geneerinenarviointiasteikko_koulutustyyppi (
    GeneerinenArviointiasteikko_id int8 not null,
    koulutustyyppi varchar(255)
);

create table geneerinenarviointiasteikko_koulutustyyppi_AUD (
    REV int4 not null,
    GeneerinenArviointiasteikko_id int8 not null,
    koulutustyyppi varchar(255) not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, GeneerinenArviointiasteikko_id, koulutustyyppi)
);

alter table geneerinenarviointiasteikko_koulutustyyppi
    add constraint FK_cvtv8n8w6xh3skimshsnvgh9e
    foreign key (GeneerinenArviointiasteikko_id)
    references geneerinenarviointiasteikko;

alter table geneerinenarviointiasteikko_koulutustyyppi_AUD
    add constraint FK_3h93ltp73n5or81oc1pujc4y1
    foreign key (REV)
    references revinfo;

alter table geneerinenarviointiasteikko_koulutustyyppi_AUD
    add constraint FK_sbqexdmp5cvv5j4i22uhyxodv
    foreign key (REVEND)
    references revinfo;
