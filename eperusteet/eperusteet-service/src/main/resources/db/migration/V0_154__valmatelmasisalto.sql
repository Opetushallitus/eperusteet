create table valmatelma_osaamisenarviointi (
  id bigint unique not null,
  kohde_id bigint,
  selite_id bigint
);

create table valmatelma_osaamisenarviointi_aud (
  id bigint not null,
  kohde_id bigint,
  selite_id bigint,
  rev integer not null,
  revtype smallint,
  revend integer
);

create table valmatelma_osaamisentavoite (
  id bigint unique not null,
  kohde_id bigint,
  selite_id bigint,
  osaamistasonkriteeri_id bigint
);

create table valmatelma_osaamisentavoite_aud (
  id bigint not null,
  kohde_id bigint,
  selite_id bigint,
  osaamistasonkriteeri_id bigint,
  rev integer not null,
  revtype smallint,
  revend integer
);




create table valmatelmatavoite_tekstipalanen (
  valmatelma_osaamisentavoite_id bigint not null,
  tekstipalanen_id bigint not null,
  tavoitteet_ORDER integer not null,
  primary key (valmatelma_osaamisentavoite_id, tavoitteet_ORDER)
);

create table valmatelmatavoite_tekstipalanen_aud (
  valmatelma_osaamisentavoite_id bigint not null,
  tekstipalanen_id bigint not null,
  tavoitteet_ORDER integer,
  rev integer not null,
  revtype smallint,
  revend integer
);

create table valmatelmaarviointi_tekstipalanen (
  valmatelma_osaamisenarviointi_id bigint not null,
  tekstipalanen_id bigint not null,
  arviointi_ORDER integer not null,
  primary key (valmatelma_osaamisenarviointi_id, arviointi_ORDER)
);

create table valmatelmaarviointi_tekstipalanen_aud (
  valmatelma_osaamisenarviointi_id bigint not null,
  tekstipalanen_id bigint not null,
  arviointi_ORDER integer,
  rev integer not null,
  revtype smallint,
  revend integer
);




create table osaalue_valmatelma (
  id bigint unique not null,
  osaamistavoite_id bigint references valmatelma_osaamisentavoite(id),
  osaamisenarviointi_id bigint references valmatelma_osaamisenarviointi(id),
  osaamisenarviointitekstina_id bigint
);

create table osaalue_valmatelma_aud (
  id bigint not null,
  osaamistavoite_id bigint,
  osaamisenarviointi_id bigint,
  osaamisenarviointitekstina_id bigint,
  rev integer not null,
  revtype smallint,
  revend integer
);


alter table tutkinnonosa_osaalue add column valmatelmasisalto_id bigint;
alter table tutkinnonosa_osaalue_aud add column valmatelmasisalto_id bigint;

alter table tutkinnonosa add column valmatelmasisalto_id bigint;
alter table tutkinnonosa_aud add column valmatelmasisalto_id bigint;



create table valmatelma_osaamisentavoite_osaalue_valmatelma (
  valmatelmasisalto_id  BIGINT  NOT NULL,
  osaamisentavoite_id BIGINT  NOT NULL,
  jarjestys             INTEGER NOT NULL,
  PRIMARY KEY (valmatelmasisalto_id, osaamisentavoite_id),
  FOREIGN KEY (valmatelmasisalto_id) REFERENCES osaalue_valmatelma (id),
  FOREIGN KEY (osaamisentavoite_id) REFERENCES valmatelma_osaamisentavoite (id)
);

create table valmatelma_osaamisentavoite_osaalue_valmatelma_aud (
  valmatelmasisalto_id  BIGINT,
  osaamisentavoite_id BIGINT,
  jarjestys             INTEGER,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  PRIMARY KEY (rev, valmatelmasisalto_id, osaamisentavoite_id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);


alter table valmatelma_osaamisentavoite add COLUMN nimi_id bigint references tekstipalanen(id);
alter table valmatelma_osaamisentavoite_aud add COLUMN nimi_id bigint;




