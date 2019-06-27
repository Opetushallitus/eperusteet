alter table muutosmaarays alter column url_id drop not null;

create table muutosmaarays_liite (
    muutosmaarays_id int8 not null,
    liite_id uuid not null,
    liitteet_KEY int4,
    primary key (muutosmaarays_id, liitteet_KEY)
);

create table muutosmaarays_liite_AUD (
    REV int4 not null,
    muutosmaarays_id int8 not null,
    liite_id uuid not null,
    liitteet_KEY int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, muutosmaarays_id, liite_id, liitteet_KEY)
);

alter table liite rename column tyyppi to mime;
alter table liite add tyyppi varchar(255) not null default 'TUNTEMATON';