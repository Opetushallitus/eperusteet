CREATE TABLE yl_tavoitteen_arviointi_temp AS
  TABLE yl_tavoitteen_arviointi;

ALTER TABLE yl_tavoitteen_arviointi DROP COLUMN valttavanOsaamisenKuvaus_id;
ALTER TABLE yl_tavoitteen_arviointi DROP COLUMN tyydyttavanOsaamisenKuvaus_id;
ALTER TABLE yl_tavoitteen_arviointi DROP COLUMN kiitettavanOsaamisenKuvaus_id;

ALTER TABLE yl_tavoitteen_arviointi_aud DROP COLUMN valttavanOsaamisenKuvaus_id;
ALTER TABLE yl_tavoitteen_arviointi_aud DROP COLUMN tyydyttavanOsaamisenKuvaus_id;
ALTER TABLE yl_tavoitteen_arviointi_aud DROP COLUMN kiitettavanOsaamisenKuvaus_id;

ALTER TABLE yl_tavoitteen_arviointi ADD COLUMN arvosana int4 default 8;
ALTER TABLE yl_tavoitteen_arviointi RENAME COLUMN hyvanosaamisenkuvaus_id TO osaamisenKuvaus_id;

ALTER TABLE yl_tavoitteen_arviointi_AUD ADD COLUMN arvosana int4 default 8;
ALTER TABLE yl_tavoitteen_arviointi_AUD RENAME COLUMN hyvanosaamisenkuvaus_id TO osaamisenKuvaus_id;

ALTER TABLE yl_opetuksen_tavoite ADD COLUMN vapaaTeksti_id int8;
ALTER TABLE yl_opetuksen_tavoite_AUD ADD COLUMN vapaaTeksti_id int8;

alter table yl_opetuksen_tavoite
    add constraint FK_1sqp85oi6r4mqe8qsm05ko0ok
    foreign key (vapaaTeksti_id)
    references tekstipalanen;
