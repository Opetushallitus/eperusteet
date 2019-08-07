create table ammattitaitovaatimukset2019 (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kohde_id int8,
    primary key (id)
);

create table ammattitaitovaatimukset2019_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kohde_id int8,
    primary key (id, REV)
);

create table ammattitaitovaatimukset2019_ammattitaitovaatimus (
    ammattitaitovaatimukset_id int8 not null,
    ammattitaitovaatimus_id int8 not null,
    jarjestys int4 not null,
    primary key (ammattitaitovaatimukset_id, ammattitaitovaatimus_id)
);

create table ammattitaitovaatimukset2019_ammattitaitovaatimus_AUD (
    REV int4 not null,
    ammattitaitovaatimukset_id int8 not null,
    ammattitaitovaatimus_id int8 not null,
    jarjestys int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, ammattitaitovaatimukset_id, ammattitaitovaatimus_id)
);

create table ammattitaitovaatimukset2019_kohdealue (
    ammattitaitovaatimukset_id int8 not null,
    kohdealue_id int8 not null,
    jarjestys int4 not null,
    primary key (ammattitaitovaatimukset_id, kohdealue_id)
);

create table ammattitaitovaatimukset2019_kohdealue_AUD (
    REV int4 not null,
    ammattitaitovaatimukset_id int8 not null,
    kohdealue_id int8 not null,
    jarjestys int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, ammattitaitovaatimukset_id, kohdealue_id)
);

create table ammattitaitovaatimus2019 (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodi_id int8,
    vaatimus_id int8,
    primary key (id)
);

create table ammattitaitovaatimus2019_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodi_id int8,
    vaatimus_id int8,
    primary key (id, REV)
);

create table ammattitaitovaatimus2019_kohdealue (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kuvaus_id int8,
    primary key (id)
);

create table ammattitaitovaatimus2019_kohdealue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kuvaus_id int8,
    primary key (id, REV)
);

create table ammattitaitovaatimus2019kohdealue_ammattitaitovaatimus (
    kohdealue_id int8 not null,
    ammattitaitovaatimus_id int8 not null,
    jarjestys int4 not null,
    primary key (kohdealue_id, ammattitaitovaatimus_id)
);

create table ammattitaitovaatimus2019kohdealue_ammattitaitovaatimus_AUD (
    REV int4 not null,
    kohdealue_id int8 not null,
    ammattitaitovaatimus_id int8 not null,
    jarjestys int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, kohdealue_id, ammattitaitovaatimus_id)
);

alter table ammattitaitovaatimus2019kohdealue_ammattitaitovaatimus 
    add constraint UK_gbdqdoo9bexrj9vdbpseyq1iv  unique (ammattitaitovaatimus_id);

alter table ammattitaitovaatimus2019kohdealue_ammattitaitovaatimus 
    add constraint FK_gbdqdoo9bexrj9vdbpseyq1iv 
    foreign key (ammattitaitovaatimus_id) 
    references ammattitaitovaatimus2019;

alter table ammattitaitovaatimus2019kohdealue_ammattitaitovaatimus 
    add constraint FK_drsunbomt9yqggbmnl6555hgv 
    foreign key (kohdealue_id) 
    references ammattitaitovaatimus2019_kohdealue;

alter table ammattitaitovaatimus2019kohdealue_ammattitaitovaatimus_AUD 
    add constraint FK_akbbuco3tt69kxna3ubfy3kf 
    foreign key (REV) 
    references revinfo;

alter table ammattitaitovaatimus2019kohdealue_ammattitaitovaatimus_AUD 
    add constraint FK_d813ff7h6oq0mh2oe26r79v9u 
    foreign key (REVEND) 
    references revinfo;

alter table ammattitaitovaatimukset2019_ammattitaitovaatimus 
    add constraint UK_dibln8meit7ynrljkyqcm4lc  unique (ammattitaitovaatimus_id);

alter table ammattitaitovaatimukset2019_kohdealue 
    add constraint UK_695x7c2emrg060dcjmsyj9p2b  unique (kohdealue_id);

alter table ammattitaitovaatimukset2019 
    add constraint FK_f34jp09gpdntpklpiqsd5vku8 
    foreign key (kohde_id) 
    references tekstipalanen;

alter table ammattitaitovaatimukset2019_AUD 
    add constraint FK_79c9hilok3lukcgyl6ld52mhp 
    foreign key (REV) 
    references revinfo;

alter table ammattitaitovaatimukset2019_AUD 
    add constraint FK_1mk3uihkritk7j6oxtd11kjne 
    foreign key (REVEND) 
    references revinfo;

alter table ammattitaitovaatimukset2019_ammattitaitovaatimus 
    add constraint FK_dibln8meit7ynrljkyqcm4lc 
    foreign key (ammattitaitovaatimus_id) 
    references ammattitaitovaatimus2019;

alter table ammattitaitovaatimukset2019_ammattitaitovaatimus 
    add constraint FK_9wmyqpu5vlxh5dcsgr8p0kmph 
    foreign key (ammattitaitovaatimukset_id) 
    references ammattitaitovaatimukset2019;

alter table ammattitaitovaatimukset2019_ammattitaitovaatimus_AUD 
    add constraint FK_37p6gs11er6p1o7huujnj3rm 
    foreign key (REV) 
    references revinfo;

alter table ammattitaitovaatimukset2019_ammattitaitovaatimus_AUD 
    add constraint FK_9bo1v6jsyv97c2o6gm7mxwcuf 
    foreign key (REVEND) 
    references revinfo;

alter table ammattitaitovaatimukset2019_kohdealue 
    add constraint FK_695x7c2emrg060dcjmsyj9p2b 
    foreign key (kohdealue_id) 
    references ammattitaitovaatimus2019_kohdealue;

alter table ammattitaitovaatimukset2019_kohdealue 
    add constraint FK_bd8y026y5e13y0lau6qqmbjbf 
    foreign key (ammattitaitovaatimukset_id) 
    references ammattitaitovaatimukset2019;

alter table ammattitaitovaatimukset2019_kohdealue_AUD 
    add constraint FK_74f2a26knl8yls1013kp1ipoy 
    foreign key (REV) 
    references revinfo;

alter table ammattitaitovaatimukset2019_kohdealue_AUD 
    add constraint FK_a4jhq9jxcjvr3e16r2qyv0atb 
    foreign key (REVEND) 
    references revinfo;


alter table ammattitaitovaatimus2019 
    add constraint FK_lwbsqv758mbk3d18ihpsxgbb2 
    foreign key (koodi_id) 
    references koodi;

alter table ammattitaitovaatimus2019 
    add constraint FK_lxcetacot5hewrgw7eswacdp3 
    foreign key (vaatimus_id) 
    references tekstipalanen;

alter table ammattitaitovaatimus2019_AUD 
    add constraint FK_a0hq7afr0dmta8ws0pl1g4gyy 
    foreign key (REV) 
    references revinfo;

alter table ammattitaitovaatimus2019_AUD 
    add constraint FK_fiwvyshcmte1r9eds3h8qsrp9 
    foreign key (REVEND) 
    references revinfo;

alter table ammattitaitovaatimus2019_kohdealue 
    add constraint FK_3biy1t6eb9nokxxijc7c77cdp 
    foreign key (kuvaus_id) 
    references tekstipalanen;

alter table ammattitaitovaatimus2019_kohdealue_AUD 
    add constraint FK_cp74yh39v2d6daa09st7xoiqb 
    foreign key (REV) 
    references revinfo;

alter table ammattitaitovaatimus2019_kohdealue_AUD 
    add constraint FK_k31p3sel4jcev5ylnxnsqk4x 
    foreign key (REVEND) 
    references revinfo;


alter table tutkinnonosa add column ammattitaitovaatimukset2019_id int8 references ammattitaitovaatimukset2019(id);
alter table tutkinnonosa_aud add column ammattitaitovaatimukset2019_id int8;
