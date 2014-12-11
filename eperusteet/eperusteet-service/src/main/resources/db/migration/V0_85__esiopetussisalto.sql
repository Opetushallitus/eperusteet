create table esiop_perusteen_sisalto (
  id int8 not null,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  peruste_id int8 not null,
  sisalto_id int8,
  primary key (id)
);

create table esiop_perusteen_sisalto_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  peruste_id int8,
  sisalto_id int8,
  primary key (id, REV)
);

alter table esiop_perusteen_sisalto
add constraint FK_esiop_perusteen_sisalto_peruste
foreign key (peruste_id)
references peruste;

alter table esiop_perusteen_sisalto
add constraint FK_esiop_perusteen_sisalto_perusteenosaviite
foreign key (sisalto_id)
references perusteenosaviite;

alter table esiop_perusteen_sisalto_AUD
add constraint FK_esiop_perusteen_sisalto_AUD_REVINFO_REV
foreign key (REV)
references revinfo;

alter table esiop_perusteen_sisalto_AUD
add constraint FK_esiop_perusteen_sisalto_AUD_REVINFO_REVEND
foreign key (REVEND)
references revinfo;
