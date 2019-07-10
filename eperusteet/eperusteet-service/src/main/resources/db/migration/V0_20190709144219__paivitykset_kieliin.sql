alter table yl_opetuksen_tavoite add column tavoitteistaJohdetutOppimisenTavoitteet_id BIGINT REFERENCES tekstipalanen(id);
alter table yl_opetuksen_tavoite_aud add column tavoitteistaJohdetutOppimisenTavoitteet_id BIGINT REFERENCES tekstipalanen(id);

alter table yl_tavoitteen_arviointi add column valttavanOsaamisenKuvaus_id BIGINT REFERENCES tekstipalanen(id);
alter table yl_tavoitteen_arviointi add column tyydyttavanOsaamisenKuvaus_id BIGINT REFERENCES tekstipalanen(id);
alter table yl_tavoitteen_arviointi add column kiitettavanOsaamisenKuvaus_id BIGINT REFERENCES tekstipalanen(id);

alter table yl_tavoitteen_arviointi_aud add column valttavanOsaamisenKuvaus_id BIGINT REFERENCES tekstipalanen(id);
alter table yl_tavoitteen_arviointi_aud add column tyydyttavanOsaamisenKuvaus_id BIGINT REFERENCES tekstipalanen(id);
alter table yl_tavoitteen_arviointi_aud add column kiitettavanOsaamisenKuvaus_id BIGINT REFERENCES tekstipalanen(id);