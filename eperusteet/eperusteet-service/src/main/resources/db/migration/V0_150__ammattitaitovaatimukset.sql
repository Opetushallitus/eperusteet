create table ammattitaitovaatimuksenkohdealue (
  id bigint not null,
  tutkinnonosa_id bigint not null,
  otsikko_id bigint,
  primary key (id),
  FOREIGN KEY (tutkinnonosa_id) REFERENCES tutkinnonosa (id),
  FOREIGN KEY (otsikko_id) REFERENCES tekstipalanen (id)
);

create table ammattitaitovaatimuksenkohde (
  id bigint not null,
  ammattitaitovaatimuksenkohdealue_id bigint not null,
  otsikko_id bigint,
  selite_id bigint,
  primary key (id),
  FOREIGN KEY (ammattitaitovaatimuksenkohdealue_id) REFERENCES ammattitaitovaatimuksenkohdealue (id),
  FOREIGN KEY (otsikko_id) REFERENCES tekstipalanen (id),
  FOREIGN KEY (selite_id) REFERENCES tekstipalanen (id)
);

create table ammattitaitovaatimus (
  id bigint not null,
  ammattitaitovaatimuksenkohde_id bigint not null,
  selite_id bigint,
  koodi varchar(20),
  jarjestys int,
  primary key (id),
  FOREIGN KEY (ammattitaitovaatimuksenkohde_id) REFERENCES ammattitaitovaatimuksenkohde (id),
  FOREIGN KEY (selite_id) REFERENCES tekstipalanen (id)
);




create table ammattitaitovaatimuksenkohdealue_aud (
  id bigint not null,
  tutkinnonosa_id bigint,
  otsikko_id bigint,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

create table ammattitaitovaatimuksenkohde_aud (
  id bigint not null,
  ammattitaitovaatimuksenkohdealue_id bigint,
  otsikko_id bigint,
  selite_id bigint,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

create table ammattitaitovaatimus_aud (
  id bigint not null,
  ammattitaitovaatimuksenkohde_id bigint,
  selite_id bigint,
  koodi varchar(20),
  jarjestys int,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

