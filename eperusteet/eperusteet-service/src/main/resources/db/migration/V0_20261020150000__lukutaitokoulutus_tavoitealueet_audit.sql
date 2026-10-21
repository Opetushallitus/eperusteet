create table lukutaitokoulutus_tjs_tavoitealueet (
    tjs_id int8 not null,
    tavoitealue_id int8 not null,
    tavoitealueet_ORDER int4 not null,
    primary key (tjs_id, tavoitealueet_ORDER)
);

create table lukutaitokoulutus_tjs_tavoitealueet_AUD (
    REV int4 not null,
    tjs_id int8 not null,
    tavoitealue_id int8 not null,
    tavoitealueet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, tjs_id, tavoitealue_id, tavoitealueet_ORDER)
);

insert into lukutaitokoulutus_tjs_tavoitealueet (tjs_id, tavoitealue_id, tavoitealueet_ORDER)
select tjs_id, id, tavoitealueet_ORDER
from lukutaitokoulutus_tavoitealue
where tjs_id is not null
  and tavoitealueet_ORDER is not null;

insert into lukutaitokoulutus_tjs_tavoitealueet_AUD (REV, tjs_id, tavoitealue_id, tavoitealueet_ORDER, REVTYPE)
select parent.REV, link.tjs_id, link.tavoitealue_id, link.tavoitealueet_ORDER, 0
from lukutaitokoulutus_tjs_tavoitealueet link
join lukutaitokoulutus_tjs_AUD parent on parent.id = link.tjs_id;

alter table lukutaitokoulutus_tjs_tavoitealueet
    add constraint UK_lukutaitokoulutus_tjs_tavoitealueet_tavoitealue unique (tavoitealue_id);

alter table lukutaitokoulutus_tavoitealue
    drop constraint FK_lukutaitokoulutus_tavoitealue_tjs;

alter table lukutaitokoulutus_tavoitealue
    drop column tjs_id;

alter table lukutaitokoulutus_tavoitealue
    drop column tavoitealueet_ORDER;

alter table lukutaitokoulutus_tjs_tavoitealueet
    add constraint FK_lukutaitokoulutus_tjs_tavoitealueet_tjs
    foreign key (tjs_id)
    references lukutaitokoulutus_tjs;

alter table lukutaitokoulutus_tjs_tavoitealueet
    add constraint FK_lukutaitokoulutus_tjs_tavoitealueet_tavoitealue
    foreign key (tavoitealue_id)
    references lukutaitokoulutus_tavoitealue;

alter table lukutaitokoulutus_tjs_tavoitealueet_AUD
    add constraint FK_lukutaitokoulutus_tjs_tavoitealueet_AUD_REV
    foreign key (REV)
    references revinfo;

alter table lukutaitokoulutus_tjs_tavoitealueet_AUD
    add constraint FK_lukutaitokoulutus_tjs_tavoitealueet_AUD_REVEND
    foreign key (REVEND)
    references revinfo;
