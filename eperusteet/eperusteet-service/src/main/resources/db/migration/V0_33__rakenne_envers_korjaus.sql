-- muutetaan tutkinnon rakenne -taulu käyttämään jointablea koska envers ei
-- osaa luoda ordercolumnia päätauluun kun käytetään OneToMany(mappedBy...).
create table rakennemoduuli_rakenneosa (
    rakennemoduuli_id int8 not null,
    rakenneosa_id int8 not null,
    osat_order int4 not null,
    primary key (rakennemoduuli_id, osat_ORDER)
);

create table rakennemoduuli_rakenneosa_AUD (
    REV int4 not null,
    rakennemoduuli_id int8 not null,
    rakenneosa_id int8 not null,
    osat_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, rakennemoduuli_id, rakenneosa_id, osat_ORDER)
);

-- olemassa olevan datan korjaus
insert into rakennemoduuli_rakenneosa(rakennemoduuli_id, rakenneosa_id, osat_order)
    select moduuli_id, id, osat_order from tutkinnon_rakenne where moduuli_id is not null;

insert into rakennemoduuli_rakenneosa_AUD(rev, rakennemoduuli_id, rakenneosa_id, osat_order, revtype, revend)
    select REV, moduuli_id, id, (row_number() over (partition by rev,moduuli_id order by id))-1, REVTYPE, REVEND from tutkinnon_rakenne_aud where moduuli_id is not null;

alter table tutkinnon_rakenne
    drop column moduuli_id,
    drop column osat_order;

alter table tutkinnon_rakenne_AUD
    drop column moduuli_id;

alter table rakennemoduuli_rakenneosa
    add constraint UK_rakennemoduuli_rakenneosa_rakenneosa_id unique (rakenneosa_id);

alter table rakennemoduuli_rakenneosa
    add constraint FK_rakennemoduuli_rakenneosa_rakenneosa_id
    foreign key (rakenneosa_id)
    references tutkinnon_rakenne;

alter table rakennemoduuli_rakenneosa
    add constraint FK_rakennemoduuli_rakenneosa_rakennemoduuli_id
    foreign key (rakennemoduuli_id)
    references tutkinnon_rakenne;

alter table rakennemoduuli_rakenneosa_AUD
    add constraint FK_rakennemoduuli_rakenneosa_AUD_REV
    foreign key (REV)
    references REVINFO;

alter table rakennemoduuli_rakenneosa_AUD
    add constraint FK_rakennemoduuli_rakenneosa_AUD_REVEND
    foreign key (REVEND)
    references REVINFO;
