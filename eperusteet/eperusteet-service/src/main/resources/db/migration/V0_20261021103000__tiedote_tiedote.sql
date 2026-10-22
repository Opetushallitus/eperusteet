create table tiedote_tiedote
(
    lahde_id bigint not null,
    kohde_id bigint not null,

    constraint pk_tiedote_tiedote
        primary key (lahde_id, kohde_id),

    constraint fk_tiedote_lahde
        foreign key (lahde_id)
            references tiedote,

    constraint fk_tiedote_kohde
        foreign key (kohde_id)
            references tiedote,

    constraint chk_tiedote_ei_itseensa
        check (lahde_id <> kohde_id)
);

create index idx_tiedote_kohde
    on tiedote_tiedote (kohde_id);


create table tiedote_tiedote_aud
(
    rev      integer not null,
    lahde_id bigint not null,
    kohde_id bigint not null,
    revtype  smallint,
    revend   integer,

    constraint pk_tiedote_tiedote_aud
        primary key (rev, lahde_id, kohde_id),

    constraint fk_tiedote_aud_rev
        foreign key (rev)
            references revinfo,

    constraint fk_tiedote_aud_revend
        foreign key (revend)
            references revinfo
);
