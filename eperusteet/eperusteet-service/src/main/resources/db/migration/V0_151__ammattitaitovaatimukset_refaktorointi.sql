
create table ammattitaitovaatimuksenkohdealue_tutkinnonosa(
  ammattitaitovaatimuksenkohdealue_id bigint not null,
  tutkinnonosa_id bigint not null,
  jarjestys integer not null,
  PRIMARY KEY (ammattitaitovaatimuksenkohdealue_id, jarjestys),
  FOREIGN KEY (ammattitaitovaatimuksenkohdealue_id) REFERENCES ammattitaitovaatimuksenkohdealue (id),
  FOREIGN KEY (tutkinnonosa_id) REFERENCES tutkinnonosa (id)
);

create table ammattitaitovaatimuksenkohdealue_osaamistavoite(
  ammattitaitovaatimuksenkohdealue_id bigint not null,
  osaamistavoite_id bigint not null,
  jarjestys integer not null,
  PRIMARY KEY (ammattitaitovaatimuksenkohdealue_id, jarjestys),
  FOREIGN KEY (ammattitaitovaatimuksenkohdealue_id) REFERENCES ammattitaitovaatimuksenkohdealue (id),
  FOREIGN KEY (osaamistavoite_id) REFERENCES osaamistavoite (id)
);



create table ammattitaitovaatimuksenkohdealue_tutkinnonosa_aud(
  ammattitaitovaatimuksenkohdealue_id bigint,
  tutkinnonosa_id bigint,
  jarjestys integer,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  PRIMARY KEY (rev, ammattitaitovaatimuksenkohdealue_id, jarjestys),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

create table ammattitaitovaatimuksenkohdealue_osaamistavoite_aud(
  ammattitaitovaatimuksenkohdealue_id bigint,
  osaamistavoite_id bigint,
  jarjestys integer,
  rev     INTEGER NOT NULL,
  revtype SMALLINT,
  revend  INTEGER,
  PRIMARY KEY (rev, ammattitaitovaatimuksenkohdealue_id, jarjestys),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);



DO $$
    DECLARE seq int8;
BEGIN
    select nextval('hibernate_sequence') into seq;
    insert into revinfo values (seq, extract(epoch from now())*1000, 'datamigraatio', null);

    insert into ammattitaitovaatimuksenkohdealue_tutkinnonosa( ammattitaitovaatimuksenkohdealue_id, tutkinnonosa_id, jarjestys )
      select id, tutkinnonosa_id, (
        select count( ammattitaitovaatimuksenkohdealue_id ) from ammattitaitovaatimuksenkohdealue_tutkinnonosa where tutkinnonosa_id = tutkinnonosa_id
      ) from ammattitaitovaatimuksenkohdealue;

    insert into ammattitaitovaatimuksenkohdealue_tutkinnonosa_aud( ammattitaitovaatimuksenkohdealue_id, tutkinnonosa_id, rev, revtype, revend, jarjestys )
      select id, tutkinnonosa_id, seq as rev, 0, null, (
        select count( ammattitaitovaatimuksenkohdealue_id ) from ammattitaitovaatimuksenkohdealue_tutkinnonosa_aud where tutkinnonosa_id = tutkinnonosa_id
      ) from ammattitaitovaatimuksenkohdealue;
END$$;


ALTER TABLE ammattitaitovaatimuksenkohdealue DROP COLUMN tutkinnonosa_id RESTRICT;
ALTER TABLE ammattitaitovaatimuksenkohdealue_aud DROP COLUMN tutkinnonosa_id RESTRICT;
