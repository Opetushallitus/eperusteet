create table peruste_kieli (
    peruste_id int8 not null,
    kieli varchar(255)
);

create table peruste_kieli_AUD (
    REV int4 not null,
    peruste_id int8 not null,
    kieli varchar(255) not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, peruste_id, kieli)
);

alter table peruste_kieli
    add constraint FK_owdd7e43rp2ci8pui3brsyo9t
    foreign key (peruste_id)
    references peruste;

alter table peruste_kieli_AUD
    add constraint FK_3vwqsljujjunirg1djsf36dcx
    foreign key (REV)
    references revinfo;

alter table peruste_kieli_AUD
    add constraint FK_3hbv3f45i89il9oeque6krrgc
    foreign key (REVEND)
    references revinfo;


alter table tutkinnonosa_osaalue
    add column kieli varchar(255);

alter table tutkinnonosa_osaalue_aud
    add column kieli varchar(255);

DO $$
    DECLARE seq int8;
BEGIN
    select nextval('hibernate_sequence') into seq;
    insert into revinfo values (seq, extract(epoch from now())*1000, 'datamigraatio', null);

    insert into peruste_kieli(peruste_id, kieli) select id, 'FI' FROM peruste;
    insert into peruste_kieli(peruste_id, kieli) select id, 'SV' FROM peruste;

    insert into peruste_kieli_aud(rev, peruste_id, kieli, revtype, revend) select seq as rev, id, 'FI', 0, null from peruste;
    insert into peruste_kieli_aud(rev, peruste_id, kieli, revtype, revend) select seq as rev, id, 'SV', 0, null from peruste;
END$$;

