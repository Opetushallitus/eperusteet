
alter table dokumentti add suoritustapakoodi varchar;
update dokumentti set suoritustapakoodi = 'OPS' where suoritustapakoodi is null;
alter table dokumentti alter column suoritustapakoodi set not null;

