create table aipevaihe_kohdealue (
    vaihe_id int8 not null,
    kohdealue_id int8 not null,
    kohdealue_order int4 not null,
    primary key (vaihe_id, kohdealue_order)
);

create table aipevaihe_kohdealue_AUD (
    REV int4 not null,
    vaihe_id int8 not null,
    kohdealue_id int8 not null,
    kohdealue_order int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, vaihe_id, kohdealue_id, kohdealue_order)
);
