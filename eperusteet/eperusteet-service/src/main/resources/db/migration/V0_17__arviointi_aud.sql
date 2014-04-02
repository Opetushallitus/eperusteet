alter table arviointi
        add column luoja varchar(255),
        add column muokkaaja varchar(255),
        add column luotu timestamp,
        add column muokattu timestamp;
        
alter table tutkinnonosa_AUD
        add column arviointi_MOD boolean;
        
create table arviointi_AUD (
        id int8 not null,
        REV int4 not null,
        REVTYPE int2,
        REVEND int4,
        luoja varchar(255),
        luotu timestamp,
        muokattu timestamp,
        muokkaaja varchar(255),
        lisatiedot_id int8,
        primary key (id, REV)
);

create table arviointi_arvioinninkohdealue_AUD (
        REV int4 not null,
        arviointi_id int8 not null,
        arvioinninkohdealue_id int8 not null,
        arvioinninKohdealueet_ORDER int4 not null,
        REVTYPE int2,
        REVEND int4,
        primary key (REV, arviointi_id, arvioinninkohdealue_id, arvioinninKohdealueet_ORDER)
);

alter table arviointi_AUD 
        add constraint FK_arviointi_AUD_REVINFO_REV 
        foreign key (REV) 
        references REVINFO;

alter table arviointi_AUD 
        add constraint FK_arviointi_AUD_REVINFO_REVEND
        foreign key (REVEND) 
        references REVINFO;
        
alter table arviointi_arvioinninkohdealue_AUD 
        add constraint FK_arviointi_arvioinninkohdealue_AUD_REVINFO_REV 
        foreign key (REV) 
        references REVINFO;

    alter table arviointi_arvioinninkohdealue_AUD 
        add constraint FK_arviointi_arvioinninkohdealue_AUD_REVINFO_REVEND 
        foreign key (REVEND) 
        references REVINFO;