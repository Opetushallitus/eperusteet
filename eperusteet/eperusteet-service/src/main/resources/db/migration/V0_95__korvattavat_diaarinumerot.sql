alter table peruste
        RENAME COLUMN siirtyma_alkaa TO siirtyma_paattyy;

alter table peruste_aud
        RENAME COLUMN siirtyma_alkaa TO siirtyma_paattyy;

create table korvattavat_diaarinumerot (
        Peruste_id int8 not null,
        diaarinumero varchar(255)
    );

create table korvattavat_diaarinumerot_AUD (
        REV int4 not null,
        REVTYPE int2 not null,
        Peruste_id int8 not null,
        SETORDINAL int4 not null,
        REVEND int4,
        diaarinumero varchar(255),
        primary key (REV, REVTYPE, Peruste_id, SETORDINAL)
);

alter table korvattavat_diaarinumerot
        add constraint FK_peruste_korvattavat_diaarinumerot
        foreign key (Peruste_id)
        references peruste;

alter table korvattavat_diaarinumerot_AUD
        add constraint FK_revinfo_rev_korvattavat_diaarinumerot_aud
        foreign key (REV)
        references revinfo;

alter table korvattavat_diaarinumerot_AUD
        add constraint FK_revinfo_revend_korvattavat_diaarinumero_aud
        foreign key (REVEND)
        references revinfo;