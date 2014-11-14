create table yl_kohdealue (
    id int8 not null,
    kuvaus_id int8,
    nimi_id int8,
    primary key (id)
);

create table yl_kohdealue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kuvaus_id int8,
    nimi_id int8,
    primary key (id, REV)
);

create table yl_opetuksen_tavoite_yl_kohdealue (
    yl_opetuksen_tavoite_id int8 not null,
    kohdealueet_id int8 not null,
    primary key (yl_opetuksen_tavoite_id, kohdealueet_id)
);

create table yl_opetuksen_tavoite_yl_kohdealue_AUD (
    REV int4 not null,
    yl_opetuksen_tavoite_id int8 not null,
    kohdealueet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, yl_opetuksen_tavoite_id, kohdealueet_id)
);

create table yl_oppiaine_yl_kohdealue (
    yl_oppiaine_id int8 not null,
    kohdealueet_id int8 not null,
    primary key (yl_oppiaine_id, kohdealueet_id)
);

create table yl_oppiaine_yl_kohdealue_AUD (
    REV int4 not null,
    yl_oppiaine_id int8 not null,
    kohdealueet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, yl_oppiaine_id, kohdealueet_id)
);

alter table yl_oppiaine_yl_kohdealue
    add constraint FK_6orp2fvyobnssq86ug53hnvig
    foreign key (kohdealueet_id)
    references yl_kohdealue;

alter table yl_oppiaine_yl_kohdealue
    add constraint FK_9w4orxffkgteg6w2k9b0jgrm0
    foreign key (yl_oppiaine_id)
    references yl_oppiaine;

alter table yl_oppiaine_yl_kohdealue_AUD
    add constraint FK_cgbyxaw5m39uvukb8w33hlah4
    foreign key (REV)
    references revinfo;

alter table yl_oppiaine_yl_kohdealue_AUD
    add constraint FK_lgbgdf8o5ncnwjjfgwqicdjmr
    foreign key (REVEND)
    references revinfo;

alter table yl_opetuksen_tavoite_yl_kohdealue
    add constraint FK_5tw80r88pwxl0vmcs6s784vwx
    foreign key (kohdealueet_id)
    references yl_kohdealue;

alter table yl_opetuksen_tavoite_yl_kohdealue
    add constraint FK_lptia5t778xl4whh4pf7110bo
    foreign key (yl_opetuksen_tavoite_id)
    references yl_opetuksen_tavoite;

alter table yl_opetuksen_tavoite_yl_kohdealue_AUD
    add constraint FK_t33clmfmu6wa5i7ow0pldyprh
    foreign key (REV)
    references revinfo;

alter table yl_opetuksen_tavoite_yl_kohdealue_AUD
    add constraint FK_dp5i9skvgbppxk83kw5hqmmwx
    foreign key (REVEND)
    references revinfo;


